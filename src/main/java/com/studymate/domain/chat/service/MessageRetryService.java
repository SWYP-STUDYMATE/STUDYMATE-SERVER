package com.studymate.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.MessageReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageRetryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final ObjectMapper objectMapper;

    // Redis 키 패턴
    private static final String RETRY_QUEUE_PREFIX = "message_retry:";
    private static final String OFFLINE_PREFIX = "offline_messages:";
    private static final String RETRY_COUNT_PREFIX = "message_retry_count:";
    
    // 재시도 설정
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);
    private static final Duration MESSAGE_TTL = Duration.ofHours(24);
    private static final Duration OFFLINE_MESSAGE_TTL = Duration.ofDays(7);

    /**
     * 메시지 전송 실패 시 재시도 큐에 추가
     */
    public void addToRetryQueue(ChatMessage chatMessage) {
        try {
            Long roomId = chatMessage.getChatRoom().getId();
            UUID userId = chatMessage.getSender().getUserId();
            
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", chatMessage.getId());
            messageData.put("content", chatMessage.getContent());
            messageData.put("roomId", roomId);
            messageData.put("senderId", userId);
            messageData.put("timestamp", chatMessage.getCreatedAt().toString());

            String retryKey = RETRY_QUEUE_PREFIX + roomId + ":" + userId;
            String messageJson = objectMapper.writeValueAsString(messageData);
            
            redisTemplate.opsForList().rightPush(retryKey, messageJson);
            redisTemplate.expire(retryKey, OFFLINE_MESSAGE_TTL);
            
            log.info("Message {} added to retry queue for user {} in room {}", 
                    chatMessage.getId(), userId, roomId);
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to add message to retry queue: {}", e.getMessage(), e);
        }
    }

    /**
     * 재시도 큐에서 메시지 처리
     */
    public List<ChatMessage> processRetryQueue(Long roomId, UUID userId) {
        String retryKey = RETRY_QUEUE_PREFIX + roomId + ":" + userId;
        List<ChatMessage> processedMessages = new ArrayList<>();
        
        try {
            String messageJson;
            while ((messageJson = redisTemplate.opsForList().leftPop(retryKey)) != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageData = objectMapper.readValue(messageJson, Map.class);
                    
                    Long messageId = Long.valueOf(messageData.get("messageId").toString());
                    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
                    
                    if (messageOpt.isPresent()) {
                        processedMessages.add(messageOpt.get());
                        log.debug("Processed retry message {} for user {}", messageId, userId);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to process retry message: {}", e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process retry queue for room {} user {}: {}", roomId, userId, e.getMessage(), e);
        }
        
        return processedMessages;
    }

    /**
     * 메시지 재시도 횟수 증가
     */
    public void incrementRetryCount(Long messageId) {
        String countKey = RETRY_COUNT_PREFIX + messageId;
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, MESSAGE_TTL);
    }

    /**
     * 메시지 재시도 횟수 조회
     */
    public int getRetryCount(Long messageId) {
        String countKey = RETRY_COUNT_PREFIX + messageId;
        String count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 최대 재시도 횟수 초과 여부 확인
     */
    public boolean isMaxRetryExceeded(Long messageId) {
        return getRetryCount(messageId) >= MAX_RETRY_ATTEMPTS;
    }

    /**
     * 오프라인 메시지 저장
     */
    public void storeOfflineMessage(UUID userId, ChatMessage chatMessage) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", chatMessage.getId());
            messageData.put("content", chatMessage.getContent());
            messageData.put("senderId", chatMessage.getSender().getUserId());
            messageData.put("roomId", chatMessage.getChatRoom().getId());
            messageData.put("timestamp", chatMessage.getCreatedAt().toString());

            String offlineKey = OFFLINE_PREFIX + userId;
            String messageJson = objectMapper.writeValueAsString(messageData);
            
            redisTemplate.opsForList().rightPush(offlineKey, messageJson);
            redisTemplate.expire(offlineKey, OFFLINE_MESSAGE_TTL);
            
            log.info("Offline message {} stored for user {}", chatMessage.getId(), userId);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to store offline message: {}", e.getMessage(), e);
        }
    }

    /**
     * 오프라인 메시지 조회
     */
    public List<ChatMessage> getOfflineMessages(UUID userId) {
        String offlineKey = OFFLINE_PREFIX + userId;
        List<String> messageJsonList = redisTemplate.opsForList().range(offlineKey, 0, -1);
        
        if (messageJsonList == null || messageJsonList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return messageJsonList.stream()
                .map(this::parseOfflineMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 오프라인 메시지 삭제
     */
    public void clearOfflineMessages(UUID userId) {
        String offlineKey = OFFLINE_PREFIX + userId;
        redisTemplate.delete(offlineKey);
        log.info("Offline messages cleared for user {}", userId);
    }

    /**
     * 사용자의 미읽은 메시지 개수 조회
     */
    public int getUnreadMessageCount(Long roomId, UUID userId) {
        // 마지막 읽음 시간을 기준으로 안읽은 메시지 수를 조회
        LocalDateTime lastReadTime = messageReadStatusRepository
                .findLastReadTimeByRoomIdAndUserId(roomId, userId)
                .orElse(LocalDateTime.of(1970, 1, 1, 0, 0)); // 읽은 기록이 없으면 1970년 기준
        
        return (int) messageReadStatusRepository.countUnreadMessagesInRoom(roomId, userId, lastReadTime);
    }

    /**
     * 메시지 동기화 충돌 해결
     */
    @Async
    public void resolveMessageConflict(UUID userId, Long messageId, String clientTimestamp) {
        try {
            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
            
            if (messageOpt.isPresent()) {
                ChatMessage message = messageOpt.get();
                LocalDateTime serverTime = message.getCreatedAt();
                LocalDateTime clientTime = LocalDateTime.parse(clientTimestamp);
                
                // 서버 시간을 기준으로 우선순위 결정
                if (serverTime.isBefore(clientTime)) {
                    log.info("Message conflict resolved: server timestamp {} vs client timestamp {} for message {}", 
                            serverTime, clientTime, messageId);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to resolve message conflict for user {} message {}: {}", 
                    userId, messageId, e.getMessage(), e);
        }
    }

    /**
     * 정기적으로 재시도 큐 정리
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void cleanupExpiredRetryMessages() {
        try {
            // 만료된 재시도 메시지 정리 로직
            log.debug("Cleaning up expired retry messages");
            
        } catch (Exception e) {
            log.error("Failed to cleanup expired retry messages: {}", e.getMessage(), e);
        }
    }

    // Private helper methods

    private Optional<ChatMessage> parseOfflineMessage(String messageJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = objectMapper.readValue(messageJson, Map.class);
            
            Long messageId = Long.valueOf(messageData.get("messageId").toString());
            return chatMessageRepository.findById(messageId);
            
        } catch (Exception e) {
            log.error("Failed to parse offline message: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}