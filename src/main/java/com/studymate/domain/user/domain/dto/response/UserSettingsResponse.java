package com.studymate.domain.user.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsResponse {
    private NotificationSettings notifications;
    private PrivacySettings privacy;
    private PreferenceSettings preferences;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationSettings {
        private boolean email;
        private boolean push;
        private boolean sms;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrivacySettings {
        private boolean profilePublic;
        private boolean showOnlineStatus;
        private boolean allowMessages;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreferenceSettings {
        private String language;
        private String timezone;
        private String theme; // "light", "dark", "auto"
    }
    
    // Helper constructor for backward compatibility
    public UserSettingsResponse(boolean emailNotifications, boolean pushNotifications, 
                               boolean smsNotifications, boolean profilePublic, 
                               boolean showOnlineStatus, boolean allowMessages, 
                               String language, String timezone, String theme) {
        this.notifications = new NotificationSettings(emailNotifications, pushNotifications, smsNotifications);
        this.privacy = new PrivacySettings(profilePublic, showOnlineStatus, allowMessages);
        this.preferences = new PreferenceSettings(language, timezone, theme);
    }
}