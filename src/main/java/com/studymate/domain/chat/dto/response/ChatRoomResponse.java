package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.dto.response.ParticipantDto;
import com.studymate.domain.chat.entity.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatRoomResponse(
        Long roomId,
        String roomName,
        java.util.List<ParticipantDto> participants,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getRoomName())
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
