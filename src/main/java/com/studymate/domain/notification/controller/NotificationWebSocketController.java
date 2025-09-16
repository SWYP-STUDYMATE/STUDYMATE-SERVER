package com.studymate.domain.notification.controller;

import com.studymate.domain.notification.domain.dto.request.MarkAsReadRequest;
import com.studymate.domain.notification.domain.dto.request.UpdateNotificationSettingsRequest;
import com.studymate.domain.notification.domain.dto.response.NotificationResponse;
import com.studymate.domain.notification.service.NotificationService;
import com.studymate.domain.notification.service.NotificationWebSocketService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final NotificationService notificationService;
    private final NotificationWebSocketService notificationWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림 읽음 처리
     */
    @MessageMapping("/notifications/mark-read")
    @SendToUser("/queue/notifications")
    public Map<String, Object> markAsRead(
            @Payload MarkAsReadRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Marking notification {} as read for user {}",
                request.getNotificationId(), principal.getUuid());

        UUID userId = principal.getUuid();
        notificationService.markAsRead(userId, request.getNotificationId());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "READ_CONFIRMATION");
        response.put("notificationId", request.getNotificationId());
        response.put("success", true);

        return response;
    }

    /**
     * 알림 설정 업데이트
     */
    @MessageMapping("/notifications/update-settings")
    @SendToUser("/queue/notifications")
    public Map<String, Object> updateSettings(
            @Payload UpdateNotificationSettingsRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("Updating notification settings for user {}", principal.getUuid());

        UUID userId = principal.getUuid();
        var preferences = notificationService.updateNotificationPreferences(
            userId,
            request.toUpdateRequest()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("type", "SETTINGS_UPDATED");
        response.put("settings", preferences);
        response.put("success", true);

        return response;
    }

    /**
     * 연결 상태 확인
     */
    @MessageMapping("/notifications/ping")
    @SendToUser("/queue/notifications")
    public Map<String, Object> ping(@AuthenticationPrincipal CustomUserDetails principal) {
        log.debug("Ping from user: {}", principal.getUuid());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "PONG");
        response.put("timestamp", System.currentTimeMillis());
        response.put("userId", principal.getUuid().toString());

        return response;
    }

    /**
     * 읽지 않은 알림 개수 요청
     */
    @MessageMapping("/notifications/unread-count")
    @SendToUser("/queue/notifications")
    public Map<String, Object> getUnreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        Long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "UNREAD_COUNT");
        response.put("count", unreadCount);

        return response;
    }

    /**
     * 최근 알림 요청
     */
    @MessageMapping("/notifications/recent")
    @SendToUser("/queue/notifications")
    public Map<String, Object> getRecentNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {

        UUID userId = principal.getUuid();
        var notifications = notificationService.getUnreadNotifications(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "RECENT_NOTIFICATIONS");
        response.put("notifications", notifications);

        return response;
    }
}