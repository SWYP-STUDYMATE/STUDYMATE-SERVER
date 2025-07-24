package com.studymate.domain.chat.controller;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageRequest request, Principal principal) {
        chatService.sendMessage(principal, request);
    }

    @MessageMapping("/chat/read/{roomId}/{messageId}")
    public void markAsRead(@DestinationVariable Long roomId, @DestinationVariable Long messageId, Principal principal) {
        chatService.markAsRead(principal, roomId, messageId);
    }
}
