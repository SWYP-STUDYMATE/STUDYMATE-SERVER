package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;

import java.security.Principal;

public interface ChatService {
    ChatRoomResponse createChatRoom(Principal principal, ChatRoomCreateRequest request);
    void sendMessage(Principal principal, ChatMessageRequest request);
    void markAsRead(Principal principal, Long roomId, Long messageId);
}
