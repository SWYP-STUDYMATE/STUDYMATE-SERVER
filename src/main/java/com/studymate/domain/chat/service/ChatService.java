package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;

import java.security.Principal;

public interface ChatService {
    void sendMessage(Principal principal, ChatMessageRequest request);
}
