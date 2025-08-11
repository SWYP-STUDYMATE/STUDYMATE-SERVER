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
@Table(name = "CHAT_MESSAGE")
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatImage> images = new ArrayList<>();

    public void addImage(ChatImage image) {
        this.images.add(image);
        image.setChatMessage(this);
    }

    public boolean hasMessage() {
        return this.message != null && !this.message.isEmpty();
    }

    public boolean hasImages() {
        return !this.images.isEmpty();
    }

    public boolean hasAudio() {
        return this.audioUrl != null && !this.audioUrl.isEmpty();
    }

    public boolean isOnlyImage() {
        return !hasMessage() && hasImages() && !hasAudio();
    }

    public boolean isOnlyMessage() {
        return hasMessage() && !hasImages() && !hasAudio();
    }

    public boolean isOnlyAudio() {
        return !hasMessage() && !hasImages() && hasAudio();
    }

    public boolean isMixed() {
        return (hasMessage() && hasImages()) || (hasMessage() && hasAudio()) || (hasImages() && hasAudio());
    }
}
