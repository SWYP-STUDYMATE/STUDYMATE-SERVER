package com.studymate.domain.user.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_status")
@Getter
@Setter
@NoArgsConstructor
public class UserStatus extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OnlineStatus status = OnlineStatus.OFFLINE;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "device_info", length = 100)
    private String deviceInfo;

    @Column(name = "is_studying", nullable = false)
    private boolean isStudying = false;

    @Column(name = "current_session_id")
    private UUID currentSessionId;

    public void setOnline(String deviceInfo) {
        this.status = OnlineStatus.ONLINE;
        this.lastSeenAt = LocalDateTime.now();
        this.deviceInfo = deviceInfo;
    }

    public void setOffline() {
        this.status = OnlineStatus.OFFLINE;
        this.lastSeenAt = LocalDateTime.now();
        this.isStudying = false;
        this.currentSessionId = null;
    }

    public void setStudying(UUID sessionId) {
        this.status = OnlineStatus.STUDYING;
        this.isStudying = true;
        this.currentSessionId = sessionId;
        this.lastSeenAt = LocalDateTime.now();
    }

    public boolean isOnline() {
        return this.status == OnlineStatus.ONLINE || this.status == OnlineStatus.STUDYING;
    }

    public enum OnlineStatus {
        ONLINE,      // 온라인 상태
        OFFLINE,     // 오프라인 상태
        STUDYING,    // 학습 세션 중
        AWAY         // 자리 비움
    }
}