package com.studymate.domain.notification.controller;

import com.studymate.domain.notification.domain.dto.request.CreateNotificationRequest;
import com.studymate.domain.notification.domain.dto.request.UpdateNotificationPreferenceRequest;
import com.studymate.domain.notification.domain.dto.response.NotificationPreferenceResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationStatsResponse;
import com.studymate.domain.notification.service.NotificationService;
import com.studymate.domain.notification.type.NotificationType;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createAndSendNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createAndSendNotification(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/template/{templateId}")
    public ResponseEntity<NotificationResponse> createNotificationFromTemplate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {
        UUID userId = principal.getUuid();
        NotificationResponse response = notificationService.createNotificationFromTemplate(
                userId, templateId, variables);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<NotificationResponse> response = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        List<NotificationResponse> response = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<NotificationResponse> response = notificationService.getNotificationsByCategory(
                userId, category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long notificationId) {
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        UUID userId = principal.getUuid();
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/category/{category}/mark-read")
    public ResponseEntity<Void> markCategoryAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category) {
        UUID userId = principal.getUuid();
        notificationService.markCategoryAsRead(userId, category);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        UUID userId = principal.getUuid();
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        NotificationPreferenceResponse response = notificationService.getNotificationPreferences(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updateNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        UUID userId = principal.getUuid();
        NotificationPreferenceResponse response = notificationService.updateNotificationPreferences(
                userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsResponse> getNotificationStats(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        NotificationStatsResponse response = notificationService.getNotificationStats(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/send-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendNotificationToUser(
            @RequestParam UUID userId,
            @RequestParam NotificationType type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestBody(required = false) Map<String, Object> data) {
        notificationService.sendNotificationToUser(userId, type, title, content, data);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendBulkNotification(
            @RequestParam List<UUID> userIds,
            @RequestParam NotificationType type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestBody(required = false) Map<String, Object> data) {
        notificationService.sendBulkNotification(userIds, type, title, content, data);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendPendingNotifications() {
        notificationService.sendPendingNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cleanupExpiredNotifications() {
        notificationService.cleanupExpiredNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup-old")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cleanupOldNotifications() {
        notificationService.cleanupOldNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/push-token")
    public ResponseEntity<Void> registerPushToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String token,
            @RequestParam String deviceType) {
        UUID userId = principal.getUuid();
        notificationService.registerPushToken(userId, token, deviceType);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/push-token")
    public ResponseEntity<Void> unregisterPushToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String token) {
        UUID userId = principal.getUuid();
        notificationService.unregisterPushToken(userId, token);
        return ResponseEntity.ok().build();
    }
}