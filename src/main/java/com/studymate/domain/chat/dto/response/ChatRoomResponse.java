package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.ChatParticipant;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record ChatRoomResponse(
    Long roomId,
    String roomName,
    List<String> participantNames,
    LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        List<String> participantNames = chatRoom.getParticipants().stream()
                .map(participant -> participant.getUser().getName())
                .collect(Collectors.toList());

        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .participantNames(participantNames)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
