package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomResponse(
    Long roomId,
    String roomName,
    LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
