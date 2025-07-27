package com.studymate.domain.chat.service;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request) {
        if (request.roomName() == null || request.roomName().trim().isEmpty()) {
            throw StudymateExceptionType.BAD_REQUEST.of("채팅방 이름은 필수입니다.");
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(request.roomName())
                .build();
        chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(chatRoom);
    }

    @Override
    public void sendMessage(ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(request.senderNickname())
                .message(request.message())
                .build();

        chatMessageRepository.save(chatMessage);

        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);
        messagingTemplate.convertAndSend("/sub/chat/room/" + request.roomId(), response);
    }
}


