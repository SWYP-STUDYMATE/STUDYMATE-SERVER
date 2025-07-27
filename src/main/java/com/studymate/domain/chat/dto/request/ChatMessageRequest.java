package com.studymate.domain.chat.dto.request;

import lombok.Builder;

@Builder
public record ChatMessageRequest(
    Long roomId,
    String senderNickname,
    String message
) {
}
