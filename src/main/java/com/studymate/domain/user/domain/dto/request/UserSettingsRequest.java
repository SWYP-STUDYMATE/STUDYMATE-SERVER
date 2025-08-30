package com.studymate.domain.user.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsRequest {
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean matchingNotifications;
    private boolean chatNotifications;
    private boolean sessionReminders;
    private String preferredLanguage;
    private String timezone;
    private boolean privateProfile;
}