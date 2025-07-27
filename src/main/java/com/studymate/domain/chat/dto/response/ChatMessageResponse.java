package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResponse(
    Long messageId,
    String senderNickname,
    String message,
    LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .messageId(chatMessage.getId())
                .senderNickname(chatMessage.getSenderNickname())
                .message(chatMessage.getMessage())
                .sentAt(chatMessage.getCreatedAt())
                .build();
    }
}
