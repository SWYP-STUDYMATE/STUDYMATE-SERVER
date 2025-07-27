package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;

public interface ChatService {
    ChatRoomResponse createChatRoom(ChatRoomCreateRequest request);
    void sendMessage(ChatMessageRequest request);
}
