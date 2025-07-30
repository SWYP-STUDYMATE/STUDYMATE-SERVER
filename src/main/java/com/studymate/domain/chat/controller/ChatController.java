// ChatController.java
package com.studymate.domain.chat.controller;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.service.ChatService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(
            Authentication authentication,          // <-- Authentication 으로 변경
            @Payload ChatMessageRequest request
    ) {
        // 인증/Principal 체크
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw StudymateExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of(
                    "인증된 사용자만 메시지 전송 가능");
        }

        // CustomUserDetails 꺼내기
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        UUID userId = UUID.fromString(userDetails.getUsername());

        // 실제 메시지 전송 로직
        chatService.sendMessage(
                request.roomId(),
                userId,
                request.message()
        );
    }
}
