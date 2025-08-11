package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.dto.response.ParticipantDto;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.RoomType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatRoomResponse(
        Long roomId,
        String roomName,
        RoomType roomType,
        boolean isPublic,
        Integer maxParticipants,
        java.util.List<ParticipantDto> participants,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getRoomName())
                .roomType(room.getRoomType())
                .isPublic(room.isPublic())
                .maxParticipants(room.getMaxParticipants())
                .participants(
                        room.getParticipants().stream()
                                .map(p -> (ParticipantDto) new ParticipantDto() {
                                    public UUID getUserId() { return p.getUser().getUserId(); }
                                    public String getName() { return p.getUser().getName(); }
                                    public String getProfileImage() { return p.getUser().getProfileImage(); }
                                })
                                .toList()
                )
                .createdAt(room.getCreatedAt())
                .build();
    }
}
