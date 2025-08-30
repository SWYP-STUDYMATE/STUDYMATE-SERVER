package com.studymate.domain.analytics.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false, length = 100)
    private String activityType; // LOGIN, LOGOUT, SESSION_JOIN, MESSAGE_SENT, PROFILE_UPDATE, etc.

    @Column(name = "activity_category", nullable = false, length = 50)
    private String activityCategory; // AUTH, SESSION, CHAT, PROFILE, LEARNING, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // JSON 형태의 추가 데이터

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 200)
    private String sessionId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // 활동 지속 시간

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    public UserActivity(User user, String activityType, String activityCategory,
                       String description, String metadata, String ipAddress,
                       String userAgent, String sessionId, Integer durationSeconds,
                       Boolean success, String errorMessage) {
        this.user = user;
        this.activityType = activityType;
        this.activityCategory = activityCategory;
        this.description = description;
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.durationSeconds = durationSeconds;
        this.success = success != null ? success : true;
        this.errorMessage = errorMessage;
    }

    public void markAsFailure(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public void updateDuration(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}