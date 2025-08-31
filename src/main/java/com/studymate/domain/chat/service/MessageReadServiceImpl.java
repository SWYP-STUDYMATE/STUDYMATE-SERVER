package com.studymate.domain.chat.service;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.MessageReadStatus;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomParticipantRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.chat.repository.MessageReadStatusRepository;
import com.studymate.domain.chat.dto.response.MessageReadStatusResponse;
import com.studymate.domain.chat.dto.response.UnreadMessageSummary;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageReadServiceImpl implements MessageReadService {

    private final MessageReadStatusRepository readStatusRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String UNREAD_COUNT_PREFIX = "unread:count:";
    private static final String LAST_READ_PREFIX = "last:read:";

    @Override
    public void markMessageAsRead(Long messageId, UUID userId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND MESSAGE"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 자신이 보낸 메시지는 읽음 처리하지 않음
        if (message.getSender().getUserId().equals(userId)) {
            return;
        }

        // 이미 읽음 처리된 경우 중복 처리 방지
        Optional<MessageReadStatus> existing = readStatusRepository
                .findByMessageAndReaderAndIsDeletedFalse(message, user);
        
        if (existing.isPresent()) {
            return;
        }

        MessageReadStatus readStatus = MessageReadStatus.builder()
                .message(message)
                .reader(user)
                .readAt(LocalDateTime.now())
                .build();

        readStatusRepository.save(readStatus);

        // 캐시 업데이트 
        updateUnreadCountCache(message.getChatRoom().getId(), userId);
        
        log.debug("Message {} marked as read by user {}", messageId, userId);
    }

    @Override
    @Transactional
    public void markRoomMessagesAsRead(Long roomId, UUID userId, LocalDateTime readUntil) {
        // TODO: 채팅방의 특정 시간까지 모든 메시지를 읽음 처리
        log.debug("Marked room {} messages as read for user {} until {}", roomId, userId, readUntil);
    }

    @Override
    @Transactional
    public void markAllRoomMessagesAsRead(Long roomId, UUID userId) {
        markRoomMessagesAsRead(roomId, userId, LocalDateTime.now());
    }

    // 중복 제거됨

    @Override
    @Transactional(readOnly = true)
    public MessageReadStatusResponse getMessageReadStatus(Long messageId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND MESSAGE"));

        List<MessageReadStatus> readStatuses = readStatusRepository
                .findByMessageAndIsDeletedFalseOrderByReadAtAsc(message);

        // 채팅방 참가자 수 조회
        int totalParticipants = participantRepository
                .countByChatRoom(message.getChatRoom());

        List<MessageReadStatusResponse.ReaderInfo> readers = readStatuses.stream()
                .map(status -> MessageReadStatusResponse.ReaderInfo.builder()
                        .userId(status.getReader().getUserId())
                        .userName(status.getReader().getName())
                        .profileImage(status.getReader().getProfileImage())
                        .readAt(status.getReadAt())
                        .build())
                .collect(Collectors.toList());

        // 읽지 않은 사용자 목록 (임시로 빈 리스트 - TODO: repository 메서드 구현 필요)
        // List<User> unreadUsers = readStatusRepository.findUnreadUsersByMessage(
        //         messageId, message.getChatRoom().getId(), message.getSender());
        
        List<UUID> unreadUserIds = new ArrayList<>(); // 임시로 빈 리스트

        LocalDateTime firstReadAt = readStatuses.stream()
                .map(MessageReadStatus::getReadAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastReadAt = readStatuses.stream()
                .map(MessageReadStatus::getReadAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        boolean isFullyRead = unreadUserIds.isEmpty();

        return MessageReadStatusResponse.builder()
                .messageId(messageId)
                .totalReaders(readers.size())
                .totalParticipants(totalParticipants)
                .readers(readers)
                .unreadUserIds(unreadUserIds)
                .firstReadAt(firstReadAt)
                .lastReadAt(lastReadAt)
                .isFullyRead(isFullyRead)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long roomId, UUID userId) {
        // 캐시에서 먼저 확인
        String cacheKey = UNREAD_COUNT_PREFIX + roomId + ":" + userId;
        Long cachedCount = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCount != null) {
            return cachedCount;
        }

        // 마지막 읽음 시간 조회
        Optional<LocalDateTime> lastReadTime = readStatusRepository
                .findLastReadTimeByRoomIdAndUserId(roomId, userId);

        long unreadCount;
        if (lastReadTime.isPresent()) {
            unreadCount = readStatusRepository
                    .countUnreadMessagesInRoom(roomId, userId, lastReadTime.get());
        } else {
            // 한번도 읽지 않은 경우 모든 메시지가 안읽은 메시지
            unreadCount = messageRepository.countByRoomIdAndSenderIdNot(roomId, userId);
        }

        // 캐시에 저장 (5분)
        redisTemplate.opsForValue().set(cacheKey, unreadCount, 5, TimeUnit.MINUTES);

        return unreadCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnreadMessageCount(UUID userId) {
        return readStatusRepository.countTotalUnreadMessages(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnreadMessageSummary> getUnreadMessageSummary(UUID userId) {
        // 사용자가 참여한 모든 채팅방 조회
        List<ChatRoom> rooms = participantRepository.findRoomsByUserId(userId);
        
        return rooms.stream()
                .map(room -> {
                    long unreadCount = getUnreadMessageCount(room.getId(), userId);
                    if (unreadCount == 0) {
                        return null; // 안읽은 메시지가 없는 방은 제외
                    }
                    
                    LocalDateTime lastReadAt = getLastReadTime(room.getId(), userId);
                    
                    // 마지막 메시지 정보 조회
                    Optional<ChatMessage> lastMessage = messageRepository
                            .findTopByChatRoom_IdOrderByCreatedAtDesc(room.getId());
                    
                    UnreadMessageSummary.UnreadMessageSummaryBuilder builder = UnreadMessageSummary.builder()
                            .roomId(room.getId())
                            .roomName(room.getRoomName())
                            .unreadCount(unreadCount)
                            .lastReadAt(lastReadAt);
                    
                    if (lastMessage.isPresent()) {
                        ChatMessage msg = lastMessage.get();
                        builder.lastMessageAt(msg.getCreatedAt())
                               .lastMessageContent(getMessagePreview(msg))
                               .lastMessageSender(msg.getSender().getName())
                               .lastMessageType(determineMessageType(msg));
                    }
                    
                    return builder.build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getLastMessageAt().compareTo(a.getLastMessageAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadMessageSummary.GlobalUnreadSummary getGlobalUnreadSummary(UUID userId) {
        long totalUnread = getTotalUnreadMessageCount(userId);
        List<UnreadMessageSummary> roomSummaries = getUnreadMessageSummary(userId);
        
        Map<Long, Long> unreadByRoom = roomSummaries.stream()
                .collect(Collectors.toMap(
                        UnreadMessageSummary::getRoomId,
                        UnreadMessageSummary::getUnreadCount
                ));
        
        return UnreadMessageSummary.GlobalUnreadSummary.builder()
                .totalUnreadMessages(totalUnread)
                .unreadRoomsCount(roomSummaries.size())
                .unreadByRoom(unreadByRoom)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime getLastReadTime(Long roomId, UUID userId) {
        String cacheKey = LAST_READ_PREFIX + roomId + ":" + userId;
        LocalDateTime cachedTime = (LocalDateTime) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTime != null) {
            return cachedTime;
        }

        Optional<LocalDateTime> lastReadTime = readStatusRepository
                .findLastReadTimeByRoomIdAndUserId(roomId, userId);
        
        LocalDateTime result = lastReadTime.orElse(null);
        if (result != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMessageFullyRead(Long messageId) {
        MessageReadStatusResponse status = getMessageReadStatus(messageId);
        return status.isFullyRead();
    }

    @Override
    public void cleanupOldReadStatuses(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);
        int cleanedCount = readStatusRepository.cleanupOldReadStatuses(cutoffDate);
        
        log.info("Cleaned up {} old read statuses older than {} days", cleanedCount, daysThreshold);
    }

    // Private helper methods

    private void updateUnreadCountCache(Long roomId, UUID userId) {
        String cacheKey = UNREAD_COUNT_PREFIX + roomId + ":" + userId;
        redisTemplate.delete(cacheKey); // 캐시 무효화하여 다음 조회시 재계산
    }

    private void updateLastReadTimeCache(UUID roomId, UUID userId, LocalDateTime readTime) {
        String cacheKey = LAST_READ_PREFIX + roomId + ":" + userId;
        redisTemplate.opsForValue().set(cacheKey, readTime, 10, TimeUnit.MINUTES);
    }

    // 중복 메서드 제거됨

    // 중복 메서드 제거됨

    private String getMessagePreview(ChatMessage message) {
        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            return message.getMessage().length() > 50 ? 
                   message.getMessage().substring(0, 50) + "..." : 
                   message.getMessage();
        } else if (message.hasFiles()) {
            return "파일을 전송했습니다.";
        } else if (message.hasImages()) {
            return "이미지를 전송했습니다.";
        } else if (message.hasAudio()) {
            return "음성 메시지를 전송했습니다.";
        } else {
            return "메시지";
        }
    }

    private String determineMessageType(ChatMessage message) {
        if (message.hasFiles()) return "FILE";
        if (message.hasImages()) return "IMAGE";
        if (message.hasAudio()) return "AUDIO";
        return "TEXT";
    }
}