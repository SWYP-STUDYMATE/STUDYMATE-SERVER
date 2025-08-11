package com.studymate.domain.chat.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomParticipant> participants = new ArrayList<>();

    public void addParticipant(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (this.id == null) {
            throw new IllegalStateException("ChatRoom must be persisted first");
        }
        if (participants.stream().anyMatch(p -> p.getUser().getUserId().equals(user.getUserId()))) {
            throw new IllegalStateException("User already joined");
        }
        
        int maxAllowed = maxParticipants != null ? maxParticipants : 
                        (roomType == RoomType.ONE_TO_ONE ? 2 : 4);
        
        if (participants.size() >= maxAllowed) {
            throw new IllegalStateException("최대 " + maxAllowed + "명까지 참여 가능합니다.");
        }
        
        participants.add(ChatRoomParticipant.builder()
                .id(new ChatRoomParticipantId(id, user.getUserId()))
                .room(this)
                .user(user)
                .build());
    }

    public boolean canJoin(UUID userId) {
        // 이미 참여 중인지 확인
        if (participants.stream().anyMatch(p -> p.getUser().getUserId().equals(userId))) {
            return false;
        }
        
        // 공개 채팅방이거나 1:1 채팅방인 경우 참여 가능
        if (isPublic || roomType == RoomType.ONE_TO_ONE) {
            int maxAllowed = maxParticipants != null ? maxParticipants : 
                            (roomType == RoomType.ONE_TO_ONE ? 2 : 4);
            return participants.size() < maxAllowed;
        }
        
        return false;
    }
}
