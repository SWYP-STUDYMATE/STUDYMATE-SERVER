package com.studymate.domain.chat.controller;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.service.ChatService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Payload ChatMessageRequest request
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        chatService.sendMessage(
                request.roomId(),
                userId,
                request.message()
        );
    }
}
