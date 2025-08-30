package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.session.type.BookingStatus;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionBooking extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "booking_message", columnDefinition = "TEXT")
    private String bookingMessage;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "attended", nullable = false)
    private Boolean attended = false;

    @Column(name = "feedback_rating")
    private Integer feedbackRating; // 1-5 stars

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Builder
    public SessionBooking(Session session, User user, String bookingMessage) {
        this.session = session;
        this.user = user;
        this.bookingMessage = bookingMessage;
        this.status = BookingStatus.CONFIRMED;
        this.attended = false;
        this.reminderSent = false;
    }

    public void cancelBooking(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void markAsAttended() {
        this.attended = true;
    }

    public void markAsNoShow() {
        this.status = BookingStatus.NO_SHOW;
    }

    public void addFeedback(Integer rating, String comment) {
        this.feedbackRating = rating;
        this.feedbackComment = comment;
    }

    public void markReminderSent() {
        this.reminderSent = true;
        this.reminderSentAt = LocalDateTime.now();
    }

    public boolean canCancel() {
        return this.status == BookingStatus.CONFIRMED && 
               this.session.getScheduledAt().isAfter(LocalDateTime.now().plusHours(1));
    }
}