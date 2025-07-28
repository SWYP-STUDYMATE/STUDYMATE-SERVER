package com.studymate.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatRoomListResponse(
        Long roomId,
        String roomName,
        List<ParticipantDto> participants,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
