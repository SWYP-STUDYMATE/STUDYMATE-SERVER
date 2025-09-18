package com.studymate.domain.notification.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.common.dto.ApiResponse;
import com.studymate.domain.notification.domain.dto.request.CreateNotificationRequest;
import com.studymate.domain.notification.domain.dto.request.UpdateNotificationPreferenceRequest;
import com.studymate.domain.notification.domain.dto.response.NotificationListResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationPreferenceResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationStatsResponse;
import com.studymate.domain.notification.service.NotificationService;
import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "type", required = false) String typeFilter,
            @RequestParam(name = "category", required = false) String categoryFilter,
            @RequestParam(name = "isRead", required = false) Boolean isRead,
            @RequestParam(name = "unreadOnly", required = false) Boolean unreadOnly
    ) {
        UUID userId = principal.getUuid();

        int safePage = Math.max(page, 1);
        int clampedSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage - 1, clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        NotificationStatus statusFilter = null;
        if (Boolean.TRUE.equals(unreadOnly)) {
            statusFilter = NotificationStatus.UNREAD;
        } else if (isRead != null) {
            statusFilter = isRead ? NotificationStatus.READ : NotificationStatus.UNREAD;
        }

        String normalizedCategory = normalizeCategoryFilter(
                categoryFilter != null ? categoryFilter : typeFilter
        );

        Page<NotificationResponse> pageResult = notificationService.getUserNotifications(
                userId,
                pageable,
                normalizedCategory,
                statusFilter
        );

        List<NotificationListResponse.NotificationItem> items = pageResult.getContent().stream()
                .map(this::toNotificationItem)
                .collect(Collectors.toList());

        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(items)
                .unreadCount(notificationService.getUnreadCount(userId))
                .pagination(NotificationListResponse.Pagination.builder()
                        .page(pageResult.getNumber() + 1)
                        .size(pageResult.getSize())
                        .totalPages(pageResult.getTotalPages())
                        .totalElements(pageResult.getTotalElements())
                        .hasNext(pageResult.hasNext())
                        .build())
                .build();

        return ApiResponse.success(response, "알림 목록을 성공적으로 조회했습니다.");
    }

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

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsReadWithPatch(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        return markAsRead(principal, notificationId);
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("모든 알림을 읽음으로 표시했습니다.");
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsReadWithPatch(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return markAllAsRead(principal);
    }

    @PostMapping("/category/{category}/mark-read")
    public ApiResponse<Void> markCategoryAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category) {
        UUID userId = principal.getUuid();
        notificationService.markCategoryAsRead(userId, category);
        return ApiResponse.success("카테고리의 모든 알림을 읽음으로 표시했습니다.");
    }

    @PatchMapping("/category/{category}/read")
    public ApiResponse<Void> markCategoryAsReadWithPatch(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String category) {
        return markCategoryAsRead(principal, category);
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        UUID userId = principal.getUuid();
        notificationService.deleteNotification(userId, notificationId);
        return ApiResponse.success("알림이 성공적으로 삭제되었습니다.");
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> deleteNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) DeleteNotificationsRequest request) {
        UUID userId = principal.getUuid();
        List<Long> notificationIds = request != null ? request.getNotificationIds() : Collections.emptyList();

        if (notificationIds == null || notificationIds.isEmpty()) {
            return ApiResponse.success("삭제할 알림이 없습니다.");
        }

        notificationIds.forEach(id -> notificationService.deleteNotification(userId, id));
        return ApiResponse.success("선택된 알림이 성공적으로 삭제되었습니다.");
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

    @GetMapping("/settings")
    public ApiResponse<NotificationPreferenceResponse> getNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return getNotificationPreferences(principal);
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

    @PatchMapping("/settings")
    public ApiResponse<NotificationPreferenceResponse> updateNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        return updateNotificationPreferences(principal, request);
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
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam(value = "deviceType", required = false) String deviceTypeParam,
            @RequestBody(required = false) RegisterPushTokenRequest body) {
        UUID userId = principal.getUuid();
        String token = tokenParam != null ? tokenParam : body != null ? body.getToken() : null;
        String deviceType = deviceTypeParam != null ? deviceTypeParam
                : body != null && body.getDeviceType() != null ? body.getDeviceType() : "web";

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("TOKEN_REQUIRED");
        }

        notificationService.registerPushToken(userId, token, deviceType);
        return ApiResponse.success("푸시 토큰이 성공적으로 등록되었습니다.");
    }

    @DeleteMapping("/push-token")
    public ApiResponse<Void> unregisterPushToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestBody(required = false) UnregisterPushTokenRequest body) {
        UUID userId = principal.getUuid();
        String token = tokenParam != null ? tokenParam : body != null ? body.getToken() : null;

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("TOKEN_REQUIRED");
        }

        notificationService.unregisterPushToken(userId, token);
        return ApiResponse.success("푸시 토큰이 성공적으로 해제되었습니다.");
    }

    private NotificationListResponse.NotificationItem toNotificationItem(NotificationResponse response) {
        String category = resolveCategory(response);
        Map<String, Object> actionData = parseActionData(response.getActionData());

        return NotificationListResponse.NotificationItem.builder()
                .id(response.getNotificationId())
                .type(resolveTypeValue(response))
                .category(category)
                .title(response.getTitle())
                .message(response.getContent())
                .content(response.getContent())
                .isRead(response.getStatus() == NotificationStatus.READ)
                .status(response.getStatus())
                .priority(response.getPriority())
                .createdAt(response.getCreatedAt())
                .readAt(response.getReadAt())
                .scheduledAt(response.getScheduledAt())
                .expiresAt(response.getExpiresAt())
                .clickUrl(response.getActionUrl())
                .data(actionData)
                .imageUrl(response.getImageUrl())
                .iconUrl(response.getIconUrl())
                .highPriority(Boolean.TRUE.equals(response.getIsHighPriority()))
                .expired(Boolean.TRUE.equals(response.getIsExpired()))
                .build();
    }

    private Map<String, Object> parseActionData(String actionData) {
        if (actionData == null || actionData.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(actionData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.debug("Failed to parse actionData: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private String resolveCategory(NotificationResponse response) {
        if (response.getCategory() != null && !response.getCategory().isBlank()) {
            return response.getCategory().toLowerCase();
        }

        if (response.getType() == null) {
            return "system";
        }

        switch (response.getType()) {
            case NEW_MESSAGE:
            case CHAT_INVITATION:
                return "chat";
            case MATCH_REQUEST_RECEIVED:
            case MATCH_REQUEST_ACCEPTED:
            case MATCH_REQUEST_REJECTED:
            case NEW_MATCH_FOUND:
                return "matching";
            case SESSION_REMINDER:
            case SESSION_CANCELLED:
            case SESSION_STARTED:
            case SESSION_COMPLETED:
                return "session";
            case LEVEL_TEST_AVAILABLE:
            case LEVEL_TEST_RESULT:
            case LEVEL_TEST_REMINDER:
                return "level_test";
            case DAILY_STREAK:
            case ACHIEVEMENT_UNLOCKED:
            case LEARNING_MILESTONE:
            case WEEKLY_SUMMARY:
                return "achievement";
            case FEATURE_ANNOUNCEMENT:
            case PROMOTIONAL_OFFER:
            case NEWSLETTER:
                return "marketing";
            case SYSTEM_MAINTENANCE:
            case SYSTEM_UPDATE:
            case ACCOUNT_SECURITY:
            case PASSWORD_CHANGED:
                return "system";
            default:
                return "general";
        }
    }

    private String resolveTypeValue(NotificationResponse response) {
        if (response.getType() == null) {
            return "system";
        }

        switch (response.getType()) {
            case NEW_MESSAGE:
            case CHAT_INVITATION:
                return "chat";
            case MATCH_REQUEST_RECEIVED:
                return "match_request";
            case MATCH_REQUEST_ACCEPTED:
                return "match_accepted";
            case MATCH_REQUEST_REJECTED:
                return "match_rejected";
            case NEW_MATCH_FOUND:
                return "match_found";
            case SESSION_REMINDER:
                return "session_reminder";
            case SESSION_CANCELLED:
                return "session_cancelled";
            case SESSION_STARTED:
                return "session_started";
            case SESSION_COMPLETED:
                return "session_completed";
            case LEVEL_TEST_AVAILABLE:
                return "level_test_available";
            case LEVEL_TEST_RESULT:
                return "level_test_result";
            case LEVEL_TEST_REMINDER:
                return "level_test_reminder";
            case DAILY_STREAK:
                return "daily_streak";
            case ACHIEVEMENT_UNLOCKED:
                return "achievement";
            case LEARNING_MILESTONE:
                return "learning_milestone";
            case WEEKLY_SUMMARY:
                return "weekly_summary";
            case SYSTEM_MAINTENANCE:
                return "system_maintenance";
            case SYSTEM_UPDATE:
                return "system_update";
            case ACCOUNT_SECURITY:
                return "account_security";
            case PASSWORD_CHANGED:
                return "password_changed";
            case FEATURE_ANNOUNCEMENT:
                return "feature_announcement";
            case PROMOTIONAL_OFFER:
                return "promotional_offer";
            case NEWSLETTER:
                return "newsletter";
            case FRIEND_REQUEST:
                return "friend_request";
            case CUSTOM:
            default:
                return "custom";
        }
    }

    private String normalizeCategoryFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim().toUpperCase();
        if (normalized.contains("-")) {
            normalized = normalized.replace('-', '_');
        }

        switch (normalized) {
            case "CHAT":
            case "MATCHING":
            case "SESSION":
            case "SYSTEM":
            case "ACHIEVEMENT":
            case "MARKETING":
            case "LEVEL_TEST":
            case "GENERAL":
                return normalized;
            default:
                return normalized;
        }
    }

    @Getter
    @Setter
    private static class DeleteNotificationsRequest {
        private List<Long> notificationIds;
    }

    @Getter
    @Setter
    private static class RegisterPushTokenRequest {
        private String token;
        private String deviceType;
        private String userAgent;
    }

    @Getter
    @Setter
    private static class UnregisterPushTokenRequest {
        private String token;
    }
}
