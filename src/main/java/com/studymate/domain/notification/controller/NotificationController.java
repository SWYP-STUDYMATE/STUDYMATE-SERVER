package com.studymate.domain.notification.controller;

import com.studymate.common.dto.ApiResponse;
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
    public ApiResponse<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ApiResponse.success(response, "알림이 성공적으로 생성되었습니다.");
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NotificationResponse> createAndSendNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createAndSendNotification(request);
        return ApiResponse.success(response, "알림이 성공적으로 생성되고 발송되었습니다.");
    }

    @PostMapping("/template/{templateId}")
    public ApiResponse<NotificationResponse> createNotificationFromTemplate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {
        UUID userId = principal.getUuid();
        NotificationResponse response = notificationService.createNotificationFromTemplate(
                userId, templateId, variables);
        return ApiResponse.success(response, "템플릿으로부터 알림이 성공적으로 생성되었습니다.");
    }

    @GetMapping("/my-notifications")
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<NotificationResponse> response = notificationService.getUserNotifications(userId, pageable);
        return ApiResponse.success(response, "내 알림 목록을 성공적으로 조회했습니다.");
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        List<NotificationResponse> response = notificationService.getUnreadNotifications(userId);
        return ApiResponse.success(response, "읽지 않은 알림 목록을 성공적으로 조회했습니다.");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<Page<NotificationResponse>> getNotificationsByCategory(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<NotificationResponse> response = notificationService.getNotificationsByCategory(
                userId, category, pageable);
        return ApiResponse.success(response, "카테고리별 알림 목록을 성공적으로 조회했습니다.");
    }

    @GetMapping("/{notificationId}")
    public ApiResponse<NotificationResponse> getNotification(
            @PathVariable Long notificationId) {
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ApiResponse.success(response, "알림 정보를 성공적으로 조회했습니다.");
    }

    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        UUID userId = principal.getUuid();
        notificationService.markAsRead(userId, notificationId);
        return ApiResponse.success("알림을 읽음으로 표시했습니다.");
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("모든 알림을 읽음으로 표시했습니다.");
    }

    @PostMapping("/category/{category}/mark-read")
    public ApiResponse<Void> markCategoryAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category) {
        UUID userId = principal.getUuid();
        notificationService.markCategoryAsRead(userId, category);
        return ApiResponse.success("카테고리의 모든 알림을 읽음으로 표시했습니다.");
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        UUID userId = principal.getUuid();
        notificationService.deleteNotification(userId, notificationId);
        return ApiResponse.success("알림이 성공적으로 삭제되었습니다.");
    }

    @DeleteMapping("/all")
    public ApiResponse<Void> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        notificationService.deleteAllNotifications(userId);
        return ApiResponse.success("모든 알림이 성공적으로 삭제되었습니다.");
    }

    @GetMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> getNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        NotificationPreferenceResponse response = notificationService.getNotificationPreferences(userId);
        return ApiResponse.success(response, "알림 설정을 성공적으로 조회했습니다.");
    }

    @PutMapping("/preferences")
    public ApiResponse<NotificationPreferenceResponse> updateNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        UUID userId = principal.getUuid();
        NotificationPreferenceResponse response = notificationService.updateNotificationPreferences(
                userId, request);
        return ApiResponse.success(response, "알림 설정이 성공적으로 업데이트되었습니다.");
    }

    @GetMapping("/stats")
    public ApiResponse<NotificationStatsResponse> getNotificationStats(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        NotificationStatsResponse response = notificationService.getNotificationStats(userId);
        return ApiResponse.success(response, "알림 통계를 성공적으로 조회했습니다.");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        Long count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(count, "읽지 않은 알림 수를 성공적으로 조회했습니다.");
    }

    @PostMapping("/send-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> sendNotificationToUser(
            @RequestParam UUID userId,
            @RequestParam NotificationType type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestBody(required = false) Map<String, Object> data) {
        notificationService.sendNotificationToUser(userId, type, title, content, data);
        return ApiResponse.success("특정 사용자에게 알림을 성공적으로 발송했습니다.");
    }

    @PostMapping("/send-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> sendBulkNotification(
            @RequestParam List<UUID> userIds,
            @RequestParam NotificationType type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestBody(required = false) Map<String, Object> data) {
        notificationService.sendBulkNotification(userIds, type, title, content, data);
        return ApiResponse.success("대량 알림을 성공적으로 발송했습니다.");
    }

    @PostMapping("/send-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> sendPendingNotifications() {
        notificationService.sendPendingNotifications();
        return ApiResponse.success("대기 중인 알림을 성공적으로 발송했습니다.");
    }

    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> cleanupExpiredNotifications() {
        notificationService.cleanupExpiredNotifications();
        return ApiResponse.success("만료된 알림을 성공적으로 정리했습니다.");
    }

    @PostMapping("/cleanup-old")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> cleanupOldNotifications() {
        notificationService.cleanupOldNotifications();
        return ApiResponse.success("오래된 알림을 성공적으로 정리했습니다.");
    }

    @PostMapping("/push-token")
    public ApiResponse<Void> registerPushToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String token,
            @RequestParam String deviceType) {
        UUID userId = principal.getUuid();
        notificationService.registerPushToken(userId, token, deviceType);
        return ApiResponse.success("푸시 토큰이 성공적으로 등록되었습니다.");
    }

    @DeleteMapping("/push-token")
    public ApiResponse<Void> unregisterPushToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String token) {
        UUID userId = principal.getUuid();
        notificationService.unregisterPushToken(userId, token);
        return ApiResponse.success("푸시 토큰이 성공적으로 해제되었습니다.");
    }
}