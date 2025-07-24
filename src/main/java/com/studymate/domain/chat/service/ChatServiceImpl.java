package com.studymate.domain.chat.service;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserDao userDao;

    @Override
    public void sendMessage(Principal principal, ChatMessageRequest request) {
        // Principal에서 UUID를 가져오는 로직 (추후 Spring Security와 연동)
        // 예시: UUID userId = UUID.fromString(principal.getName());
        // 현재는 임시로 UUID를 사용
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
}
