package com.studymate.domain.notification.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preferenceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 전체 알림 설정
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    // 카테고리별 알림 설정
    @Column(name = "session_notifications", nullable = false)
    private Boolean sessionNotifications = true;

    @Column(name = "session_reminders", nullable = false)
    private Boolean sessionReminders = true;

    @Column(name = "matching_notifications", nullable = false)
    private Boolean matchingNotifications = true;

    @Column(name = "chat_notifications", nullable = false)
    private Boolean chatNotifications = true;

    @Column(name = "level_test_notifications", nullable = false)
    private Boolean levelTestNotifications = true;

    @Column(name = "system_notifications", nullable = false)
    private Boolean systemNotifications = true;

    @Column(name = "marketing_notifications", nullable = false)
    private Boolean marketingNotifications = false;

    // 시간대 설정
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start", length = 5)
    private String quietHoursStart; // HH:MM 형식

    @Column(name = "quiet_hours_end", length = 5)
    private String quietHoursEnd; // HH:MM 형식

    @Column(name = "timezone", length = 50)
    private String timezone; // Asia/Seoul, America/New_York, etc.

    // 언어 설정
    @Column(name = "notification_language", length = 10)
    private String notificationLanguage = "ko"; // ko, en, ja, zh, etc.

    // 빈도 설정
    @Column(name = "digest_enabled", nullable = false)
    private Boolean digestEnabled = false; // 요약 알림

    @Column(name = "digest_frequency", length = 20)
    private String digestFrequency = "DAILY"; // DAILY, WEEKLY

    @Column(name = "digest_time", length = 5)
    private String digestTime = "09:00"; // HH:MM 형식

    @Builder
    public NotificationPreference(User user, Boolean notificationsEnabled, Boolean pushEnabled,
                                 Boolean emailEnabled, Boolean smsEnabled, String timezone,
                                 String notificationLanguage) {
        this.user = user;
        this.notificationsEnabled = notificationsEnabled != null ? notificationsEnabled : true;
        this.pushEnabled = pushEnabled != null ? pushEnabled : true;
        this.emailEnabled = emailEnabled != null ? emailEnabled : true;
        this.smsEnabled = smsEnabled != null ? smsEnabled : false;
        this.sessionNotifications = true;
        this.sessionReminders = true;
        this.matchingNotifications = true;
        this.chatNotifications = true;
        this.levelTestNotifications = true;
        this.systemNotifications = true;
        this.marketingNotifications = false;
        this.quietHoursEnabled = false;
        this.timezone = timezone;
        this.notificationLanguage = notificationLanguage != null ? notificationLanguage : "ko";
        this.digestEnabled = false;
        this.digestFrequency = "DAILY";
        this.digestTime = "09:00";
    }

    public void updateGeneralSettings(Boolean notificationsEnabled, Boolean pushEnabled,
                                    Boolean emailEnabled, Boolean smsEnabled) {
        if (notificationsEnabled != null) this.notificationsEnabled = notificationsEnabled;
        if (pushEnabled != null) this.pushEnabled = pushEnabled;
        if (emailEnabled != null) this.emailEnabled = emailEnabled;
        if (smsEnabled != null) this.smsEnabled = smsEnabled;
    }

    public void updateCategorySettings(Boolean sessionNotifications, Boolean sessionReminders,
                                     Boolean matchingNotifications, Boolean chatNotifications,
                                     Boolean levelTestNotifications, Boolean systemNotifications,
                                     Boolean marketingNotifications) {
        if (sessionNotifications != null) this.sessionNotifications = sessionNotifications;
        if (sessionReminders != null) this.sessionReminders = sessionReminders;
        if (matchingNotifications != null) this.matchingNotifications = matchingNotifications;
        if (chatNotifications != null) this.chatNotifications = chatNotifications;
        if (levelTestNotifications != null) this.levelTestNotifications = levelTestNotifications;
        if (systemNotifications != null) this.systemNotifications = systemNotifications;
        if (marketingNotifications != null) this.marketingNotifications = marketingNotifications;
    }

    public void updateQuietHours(Boolean quietHoursEnabled, String quietHoursStart,
                               String quietHoursEnd, String timezone) {
        if (quietHoursEnabled != null) this.quietHoursEnabled = quietHoursEnabled;
        if (quietHoursStart != null) this.quietHoursStart = quietHoursStart;
        if (quietHoursEnd != null) this.quietHoursEnd = quietHoursEnd;
        if (timezone != null) this.timezone = timezone;
    }

    public void updateDigestSettings(Boolean digestEnabled, String digestFrequency,
                                   String digestTime) {
        if (digestEnabled != null) this.digestEnabled = digestEnabled;
        if (digestFrequency != null) this.digestFrequency = digestFrequency;
        if (digestTime != null) this.digestTime = digestTime;
    }

    public void updateLanguage(String notificationLanguage) {
        if (notificationLanguage != null) this.notificationLanguage = notificationLanguage;
    }

    public boolean shouldReceiveNotification(String category) {
        if (!notificationsEnabled) return false;

        switch (category.toUpperCase()) {
            case "SESSION":
                return sessionNotifications;
            case "SESSION_REMINDER":
                return sessionReminders;
            case "MATCHING":
                return matchingNotifications;
            case "CHAT":
                return chatNotifications;
            case "LEVEL_TEST":
                return levelTestNotifications;
            case "SYSTEM":
                return systemNotifications;
            case "MARKETING":
                return marketingNotifications;
            default:
                return true;
        }
    }

    public boolean isInQuietHours() {
        if (!quietHoursEnabled || quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        // 실제 구현에서는 사용자 시간대를 고려한 현재 시간 계산 필요
        // 현재는 간단한 로직만 제공
        return false;
    }
}