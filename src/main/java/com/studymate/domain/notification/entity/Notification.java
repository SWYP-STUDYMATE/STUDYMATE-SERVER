package com.studymate.domain.notification.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_data", columnDefinition = "JSON")
    private String actionData; // JSON 형태의 액션 데이터

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // 1: LOW, 2: NORMAL, 3: HIGH, 4: URGENT

    @Column(name = "category", length = 50)
    private String category; // SYSTEM, SESSION, MATCHING, CHAT, LEVEL_TEST, etc.

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt; // 예약 발송 시간

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_persistent", nullable = false)
    private Boolean isPersistent = true; // 영구 보관 여부

    @Column(name = "sender_user_id")
    private String senderUserId; // 발송자 (시스템인 경우 null)

    @Column(name = "template_id", length = 100)
    private String templateId; // 알림 템플릿 ID

    @Column(name = "template_variables", columnDefinition = "JSON")
    private String templateVariables; // 템플릿 변수

    @Column(name = "delivery_channels", length = 100)
    private String deliveryChannels; // PUSH, EMAIL, SMS (콤마 구분)

    @Column(name = "push_sent", nullable = false)
    private Boolean pushSent = false;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "sms_sent", nullable = false)
    private Boolean smsSent = false;

    @Builder
    public Notification(User user, NotificationType type, String title, String content,
                       String actionUrl, String actionData, String imageUrl, String iconUrl,
                       Integer priority, String category, LocalDateTime scheduledAt,
                       LocalDateTime expiresAt, Boolean isPersistent, String senderUserId,
                       String templateId, String templateVariables, String deliveryChannels) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.actionUrl = actionUrl;
        this.actionData = actionData;
        this.imageUrl = imageUrl;
        this.iconUrl = iconUrl;
        this.priority = priority != null ? priority : 1;
        this.category = category;
        this.scheduledAt = scheduledAt;
        this.expiresAt = expiresAt;
        this.isPersistent = isPersistent != null ? isPersistent : true;
        this.senderUserId = senderUserId;
        this.templateId = templateId;
        this.templateVariables = templateVariables;
        this.deliveryChannels = deliveryChannels;
        this.status = NotificationStatus.UNREAD;
        this.pushSent = false;
        this.emailSent = false;
        this.smsSent = false;
    }

    public void markAsRead() {
        if (this.status == NotificationStatus.UNREAD) {
            this.status = NotificationStatus.READ;
            this.readAt = LocalDateTime.now();
        }
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
    }

    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
    }

    public void markPushSent() {
        this.pushSent = true;
    }

    public void markEmailSent() {
        this.emailSent = true;
    }

    public void markSmsSent() {
        this.smsSent = true;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean shouldSendNow() {
        return scheduledAt == null || LocalDateTime.now().isAfter(scheduledAt);
    }

    public boolean isHighPriority() {
        return priority >= 3;
    }
}