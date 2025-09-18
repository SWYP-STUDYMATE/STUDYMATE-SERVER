package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.session.type.SessionStatus;
import com.studymate.domain.session.type.SessionType;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_user_id")
    private User guestUser;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "skill_focus", length = 50)
    private String skillFocus; // SPEAKING, LISTENING, READING, WRITING

    @Column(name = "level_requirement", length = 20)
    private String levelRequirement; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(name = "meeting_password", length = 100)
    private String meetingPassword;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Builder
    public Session(User hostUser, String title, String description, SessionType sessionType,
                  String languageCode, String skillFocus, String levelRequirement,
                  LocalDateTime scheduledAt, Integer durationMinutes, Integer maxParticipants,
                  Boolean isRecurring, String recurrencePattern, LocalDateTime recurrenceEndDate,
                  Boolean isPublic, String tags, String preparationNotes, String meetingUrl) {
        this.hostUser = hostUser;
        this.title = title;
        this.description = description;
        this.sessionType = sessionType;
        this.languageCode = languageCode;
        this.skillFocus = skillFocus;
        this.levelRequirement = levelRequirement;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.isRecurring = isRecurring != null ? isRecurring : false;
        this.recurrencePattern = recurrencePattern;
        this.recurrenceEndDate = recurrenceEndDate;
        this.isPublic = isPublic != null ? isPublic : true;
        this.tags = tags;
        this.preparationNotes = preparationNotes;
        this.status = SessionStatus.SCHEDULED;
        this.currentParticipants = 0;
        this.meetingUrl = meetingUrl;
    }

    public void addParticipant(User user) {
        if (this.guestUser == null) {
            this.guestUser = user;
            this.currentParticipants = 1;
        } else {
            this.currentParticipants++;
        }
    }

    public void removeParticipant() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
            if (this.currentParticipants == 0) {
                this.guestUser = null;
            }
        }
    }

    public void startSession() {
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void endSession() {
        this.status = SessionStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    public void cancelSession(String reason) {
        this.status = SessionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void updateMeetingInfo(String meetingUrl, String meetingPassword) {
        this.meetingUrl = meetingUrl;
        this.meetingPassword = meetingPassword;
    }

    public boolean canJoin(User user) {
        return this.status == SessionStatus.SCHEDULED && 
               this.currentParticipants < this.maxParticipants &&
               !user.getUserId().equals(this.hostUser.getUserId());
    }

    public boolean isHost(User user) {
        return this.hostUser.getUserId().equals(user.getUserId());
    }

    public boolean isParticipant(User user) {
        return this.guestUser != null && this.guestUser.getUserId().equals(user.getUserId());
    }
}
