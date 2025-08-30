package com.studymate.domain.chat.entity;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatRoomParticipantId implements Serializable {
    @Column(name = "ROOM_ID")
    private Long roomId;
    
    @Column(name = "USER_ID")
    private UUID userId;
}
