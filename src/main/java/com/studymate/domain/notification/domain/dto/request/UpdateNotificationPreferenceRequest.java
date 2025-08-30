package com.studymate.domain.notification.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNotificationPreferenceRequest {
    
    // 전체 알림 설정
    private Boolean notificationsEnabled;
    private Boolean pushEnabled;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    
    // 카테고리별 알림 설정
    private Boolean sessionNotifications;
    private Boolean sessionReminders;
    private Boolean matchingNotifications;
    private Boolean chatNotifications;
    private Boolean levelTestNotifications;
    private Boolean systemNotifications;
    private Boolean marketingNotifications;
    
    // 시간대 설정
    private Boolean quietHoursEnabled;
    private String quietHoursStart; // HH:MM 형식
    private String quietHoursEnd;   // HH:MM 형식
    private String timezone;        // Asia/Seoul, America/New_York, etc.
    
    // 언어 설정
    private String notificationLanguage; // ko, en, ja, zh, etc.
    
    // 빈도 설정
    private Boolean digestEnabled;
    private String digestFrequency; // DAILY, WEEKLY
    private String digestTime;      // HH:MM 형식
}