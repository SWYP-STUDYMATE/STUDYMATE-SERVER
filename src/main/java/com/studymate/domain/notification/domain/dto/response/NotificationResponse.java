package com.studymate.domain.notification.domain.dto.response;

import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private NotificationType type;
    private String title;
    private String content;
    private String actionUrl;
    private String actionData;
    private String imageUrl;
    private String iconUrl;
    private NotificationStatus status;
    private Integer priority;
    private String category;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean isPersistent;
    private String senderUserId;
    private String templateId;
    private String deliveryChannels;
    private Boolean pushSent;
    private Boolean emailSent;
    private Boolean smsSent;
    private Boolean isExpired;
    private Boolean isHighPriority;
}