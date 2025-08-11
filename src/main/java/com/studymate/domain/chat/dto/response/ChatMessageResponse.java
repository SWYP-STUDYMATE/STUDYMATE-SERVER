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
        String audioUrl,
        MessageType messageType,
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage msg) {
        MessageType messageType;
        if (msg.isOnlyAudio()) {
            messageType = MessageType.AUDIO;
        } else if (msg.isOnlyImage()) {
            messageType = MessageType.IMAGE;
        } else if (msg.isOnlyMessage()) {
            messageType = MessageType.TEXT;
        } else {
            messageType = MessageType.MIXED;
        }

        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .sender(new ParticipantDto() {
                    public UUID getUserId() { return msg.getSender().getUserId(); }
                    public String getName() { return msg.getSender().getName(); }
                    public String getProfileImage() { return msg.getSender().getProfileImage(); }
                })
                .message(msg.getMessage())
                .imageUrls(msg.getImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()))
                .audioUrl(msg.getAudioUrl())
                .messageType(messageType)
                .sentAt(msg.getCreatedAt())
                .build();
    }
}
