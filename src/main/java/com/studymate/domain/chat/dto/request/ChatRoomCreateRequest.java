package com.studymate.domain.chat.dto.request;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ChatRoomCreateRequest(
    List<UUID> participantUserIds,
    String roomName
) {
}
