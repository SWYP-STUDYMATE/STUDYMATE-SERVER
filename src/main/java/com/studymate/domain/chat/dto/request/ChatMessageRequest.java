package com.studymate.domain.chat.dto.request;

import com.studymate.domain.chat.entity.MessageType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ChatMessageRequest(
    Long roomId,
    String message,
    List<String> imageUrls,
    String audioData,
    MessageType messageType
) {
}
