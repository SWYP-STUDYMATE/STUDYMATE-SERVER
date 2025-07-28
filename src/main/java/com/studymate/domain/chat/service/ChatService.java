package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatRoomListResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatRoomResponse createChatRoom(UUID creatorId, ChatRoomCreateRequest request);
    void sendMessage(Long roomId, UUID senderId, String message);
    List<ChatRoomListResponse> listChatRooms(UUID userId);
}
