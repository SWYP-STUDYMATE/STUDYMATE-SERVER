package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;

import java.util.UUID;

public interface ChatService {
    ChatRoomResponse createChatRoom(UUID creatorId, ChatRoomCreateRequest request);
    void sendMessage(Long roomId, UUID senderId, String message);
}
