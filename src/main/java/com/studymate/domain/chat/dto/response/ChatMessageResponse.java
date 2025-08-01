package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record ChatMessageResponse(
        Long messageId,
        ParticipantDto sender,
        String message,
        List<String> imageUrls,
        MessageType messageType,
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
                .imageUrls(msg.getImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()))
                .messageType(msg.isOnlyImage() ? MessageType.IMAGE : (msg.isOnlyMessage() ? MessageType.TEXT : MessageType.MIXED))
                .sentAt(msg.getCreatedAt())
                .build();
    }
}
