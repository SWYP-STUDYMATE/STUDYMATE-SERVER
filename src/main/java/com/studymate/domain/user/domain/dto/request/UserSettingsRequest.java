package com.studymate.domain.user.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsRequest {
    private NotificationSettings notifications;
    private PrivacySettings privacy;
    private PreferenceSettings preferences;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationSettings {
        private Boolean email;
        private Boolean push;
        private Boolean sms;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrivacySettings {
        private Boolean profilePublic;
        private Boolean showOnlineStatus;
        private Boolean allowMessages;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreferenceSettings {
        private String language;
        private String timezone;
        private String theme; // "light", "dark", "auto"
    }
}