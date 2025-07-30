package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        Long messageId,
        ParticipantDto sender,
        String message,
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .sender(new ParticipantDto() {
                    public UUID getUserId() { return msg.getSender().getUserId(); }
                    public String getName() { return msg.getSender().getName(); }
                    public String getProfileImage() { return msg.getSender().getProfileImage(); }
                })
                .message(msg.getMessage())
                .sentAt(msg.getCreatedAt())
                .build();
    }
}
