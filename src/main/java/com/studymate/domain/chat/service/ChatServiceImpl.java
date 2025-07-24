package com.studymate.domain.chat.service;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatParticipant;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatParticipantRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserDao userDao;

    @Override
    public ChatRoomResponse createChatRoom(Principal principal, ChatRoomCreateRequest request) {
        // 현재 로그인된 사용자 ID를 participantUserIds에 추가 (본인 포함)
        UUID currentUserId = UUID.fromString("f4b1b1b1-f1b1-11e1-b1b1-0002a5d5c51b"); // 임시 UUID
        List<UUID> allParticipantIds = new java.util.ArrayList<>(request.participantUserIds());
        if (!allParticipantIds.contains(currentUserId)) {
            allParticipantIds.add(currentUserId);
        }

        if (allParticipantIds.size() < 2 || allParticipantIds.size() > 4) {
            throw StudymateExceptionType.BAD_REQUEST.of("채팅방 참여자는 2명 이상 4명 이하여야 합니다.");
        }

        List<User> participants = allParticipantIds.stream()
                .map(userId -> userDao.findByUserId(userId)
                        .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of("참여자를 찾을 수 없습니다: " + userId)))
                .collect(Collectors.toList());

        String roomName = request.roomName();
        if (roomName == null || roomName.trim().isEmpty()) {
            // 1:1 채팅방 이름 자동 생성
            roomName = participants.stream()
                    .map(User::getName)
                    .collect(Collectors.joining(", "));
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .build();
        chatRoomRepository.save(chatRoom);

        for (User user : participants) {
            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .build();
            chatParticipantRepository.save(participant);
            chatRoom.getParticipants().add(participant); // 양방향 관계 설정
        }

        return ChatRoomResponse.from(chatRoom);
    }

    @Override
    public void sendMessage(Principal principal, ChatMessageRequest request) {
        // Principal에서 UUID를 가져오는 로직 (추후 Spring Security와 연동)
        // 예시: UUID userId = UUID.fromString(principal.getName());
        // 현재는 임시로 UUID를 사용합니다.
        UUID userId = UUID.fromString("f4b1b1b1-f1b1-11e1-b1b1-0002a5d5c51b"); // 임시 UUID

        User sender = userDao.findByUserId(userId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());

        ChatRoom chatRoom = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(request.message())
                .build();

        chatMessageRepository.save(chatMessage);

        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);
        messagingTemplate.convertAndSend("/sub/chat/room/" + request.roomId(), response);
    }

    @Override
    public void markAsRead(Principal principal, Long roomId, Long messageId) {
        UUID userId = UUID.fromString("f4b1b1b1-f1b1-11e1-b1b1-0002a5d5c51b"); // 임시 UUID

        User user = userDao.findByUserId(userId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());

        ChatParticipant participant = chatParticipantRepository.findByChatRoom_IdAndUser_UserId(roomId, user.getUserId())
                .orElseThrow(() -> StudymateExceptionType.BAD_REQUEST.of("채팅방 참여자를 찾을 수 없습니다."));

        participant.updateLastReadMessageId(messageId);
        chatParticipantRepository.save(participant);
    }
}


