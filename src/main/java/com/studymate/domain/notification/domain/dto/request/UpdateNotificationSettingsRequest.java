package com.studymate.domain.notification.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingsRequest {
    private Boolean pushEnabled;
    private Boolean emailEnabled;
    private Boolean systemNotifications;
    private Boolean matchingNotifications;
    private Boolean sessionNotifications;
    private Boolean chatNotifications;
    private String quietHoursStart;
    private String quietHoursEnd;

    public UpdateNotificationPreferenceRequest toUpdateRequest() {
        UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest();
        request.setPushEnabled(this.pushEnabled);
        request.setEmailEnabled(this.emailEnabled);
        request.setSystemNotifications(this.systemNotifications);
        request.setMatchingNotifications(this.matchingNotifications);
        request.setSessionNotifications(this.sessionNotifications);
        request.setChatNotifications(this.chatNotifications);
        request.setQuietHoursStart(this.quietHoursStart);
        request.setQuietHoursEnd(this.quietHoursEnd);
        return request;
    }
}