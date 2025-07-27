package com.studymate.domain.chat.dto.request;

import lombok.Builder;

@Builder
public record ChatRoomCreateRequest(
    String roomName
) {
}
