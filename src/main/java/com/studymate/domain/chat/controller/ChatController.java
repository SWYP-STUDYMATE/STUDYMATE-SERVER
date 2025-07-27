package com.studymate.domain.chat.controller;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageRequest request) {
        chatService.sendMessage(request);
    }
}
