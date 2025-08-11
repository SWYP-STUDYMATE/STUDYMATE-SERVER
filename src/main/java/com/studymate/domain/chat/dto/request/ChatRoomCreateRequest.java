package com.studymate.domain.chat.dto.request;

import com.studymate.domain.chat.entity.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ChatRoomCreateRequest(
    String roomName,
    RoomType roomType,
    boolean isPublic,
    Integer maxParticipants,
    List<UUID> participantIds
) {}
