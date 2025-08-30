package com.studymate.domain.matching.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching_queue")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQueue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status", nullable = false)
    private QueueStatus status;

    @Column(name = "priority_score")
    private Integer priorityScore; // 매칭 우선순위 점수

    @Column(name = "target_language", length = 10)
    private String targetLanguage;

    @Column(name = "language_level", length = 20)
    private String languageLevel;

    @Column(name = "preferred_session_duration")
    private Integer preferredSessionDuration; // 분 단위

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @Column(name = "matching_preferences", columnDefinition = "JSON")
    private String matchingPreferences; // JSON 형태로 저장된 세부 매칭 선호도

    public enum SessionType {
        CHAT_ONLY("채팅만"),
        VOICE_CALL("음성 통화"),
        VIDEO_CALL("화상 통화"),
        ANY("무관");

        private final String description;

        SessionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum QueueStatus {
        WAITING("대기 중"),
        MATCHED("매칭됨"),
        EXPIRED("만료됨"),
        CANCELLED("취소됨");

        private final String description;

        QueueStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void updateStatus(QueueStatus newStatus) {
        this.status = newStatus;
    }

    public void updateEstimatedWaitTime(Integer minutes) {
        this.estimatedWaitMinutes = minutes;
    }

    public void incrementPriority() {
        if (this.priorityScore == null) {
            this.priorityScore = 1;
        } else {
            this.priorityScore++;
        }
    }

    public boolean isActive() {
        return QueueStatus.WAITING.equals(this.status);
    }

    public boolean isExpired(int maxWaitMinutes) {
        if (joinedAt == null) return false;
        return LocalDateTime.now().minusMinutes(maxWaitMinutes).isAfter(joinedAt);
    }
}