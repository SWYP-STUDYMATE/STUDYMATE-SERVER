package com.studymate.domain.chat.entity;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatRoomParticipantId implements Serializable {
    private Long roomId;
    private UUID userId;
}
