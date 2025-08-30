package com.studymate.domain.notification.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPreferenceResponse {
    
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
    private String quietHoursStart;
    private String quietHoursEnd;
    private String timezone;
    
    // 언어 설정
    private String notificationLanguage;
    
    // 빈도 설정
    private Boolean digestEnabled;
    private String digestFrequency;
    private String digestTime;
}