package com.studymate.domain.chat.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CHAT_ROOM")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomParticipant> participants = new ArrayList<>();

    public void addParticipant(User user) {
        if (participants.size() >= 4) {
            throw new IllegalStateException("최대 4명까지 참여 가능합니다.");
        }
        participants.add(ChatRoomParticipant.builder()
                .id(new ChatRoomParticipantId(id, user.getUserId()))
                .room(this)
                .user(user)
                .build());
    }
}
