package com.studymate.domain.chat.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ChatMessageRequest(
    Long roomId,
    String message
) {
}
