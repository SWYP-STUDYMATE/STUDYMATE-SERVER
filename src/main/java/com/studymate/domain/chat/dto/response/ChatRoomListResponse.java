package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.RoomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatRoomListResponse(
        Long roomId,
        String roomName,
        RoomType roomType,
        boolean isPublic,
        Integer maxParticipants,
        List<ParticipantDto> participants,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
