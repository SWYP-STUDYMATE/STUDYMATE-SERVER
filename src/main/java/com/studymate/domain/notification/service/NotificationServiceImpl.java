package com.studymate.domain.notification.service;

import com.studymate.domain.notification.domain.dto.request.CreateNotificationRequest;
import com.studymate.domain.notification.domain.dto.request.UpdateNotificationPreferenceRequest;
import com.studymate.domain.notification.domain.dto.response.NotificationPreferenceResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationStatsResponse;
import com.studymate.domain.notification.domain.repository.NotificationPreferenceRepository;
import com.studymate.domain.notification.domain.repository.NotificationRepository;
import com.studymate.domain.notification.entity.Notification;
import com.studymate.domain.notification.entity.NotificationPreference;
import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketService webSocketService;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 중복 알림 확인
        if (isDuplicateNotification(request)) {
            log.debug("Duplicate notification prevented for user: {}, type: {}", 
                     request.getUserId(), request.getType());
            return null;
        }

        // 일일 알림 수 제한 확인
        if (isExceededDailyLimit(request.getUserId())) {
            log.warn("Daily notification limit exceeded for user: {}", request.getUserId());
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .actionUrl(request.getActionUrl())
                .actionData(request.getActionData())
                .imageUrl(request.getImageUrl())
                .iconUrl(request.getIconUrl())
                .priority(request.getPriority())
                .category(request.getCategory())
                .scheduledAt(request.getScheduledAt())
                .expiresAt(request.getExpiresAt())
                .isPersistent(request.getIsPersistent())
                .senderUserId(request.getSenderUserId())
                .templateId(request.getTemplateId())
                .templateVariables(request.getTemplateVariables())
                .deliveryChannels(request.getDeliveryChannels())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return convertToNotificationResponse(savedNotification);
    }

    @Override
    public NotificationResponse createAndSendNotification(CreateNotificationRequest request) {
        NotificationResponse response = createNotification(request);
        if (response != null) {
            // 즉시 발송 처리
            sendNotificationById(response.getNotificationId());
        }
        return response;
    }

    @Override
    public NotificationResponse createNotificationFromTemplate(UUID userId, String templateId, 
                                                             Map<String, Object> variables) {
        // 템플릿 기반 알림 생성 로직
        CreateNotificationRequest request = buildNotificationFromTemplate(userId, templateId, variables);
        return createNotification(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(this::convertToNotificationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        List<Notification> notifications = notificationRepository.findByUserAndStatusOrderByCreatedAtDesc(
                user, NotificationStatus.UNREAD);
        
        return notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByCategory(UUID userId, String category, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndCategory(userId, category, pageable);
        return notifications.map(this::convertToNotificationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND NOTIFICATION"));
        
        return convertToNotificationResponse(notification);
    }

    @Override
    public void markAsRead(UUID userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND NOTIFICATION"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("UNAUTHORIZED ACCESS");
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        // WebSocket으로 읽음 상태 업데이트 알림
        webSocketService.sendNotificationStatusUpdate(userId, notificationId, "READ");

        // 읽지 않은 알림 개수 업데이트
        Long unreadCount = getUnreadCount(userId);
        webSocketService.sendUnreadCountUpdate(userId, unreadCount);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.debug("Marked {} notifications as read for user: {}", updatedCount, userId);
    }

    @Override
    public void markCategoryAsRead(UUID userId, String category) {
        int updatedCount = notificationRepository.markCategoryAsReadByUserId(userId, category, LocalDateTime.now());
        log.debug("Marked {} notifications in category {} as read for user: {}", updatedCount, category, userId);
    }

    @Override
    public void deleteNotification(UUID userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND NOTIFICATION"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("UNAUTHORIZED ACCESS");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void deleteAllNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        List<Notification> notifications = notificationRepository.findByUserAndStatusOrderByCreatedAtDesc(
                user, NotificationStatus.UNREAD);
        notificationRepository.deleteAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getNotificationPreferences(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        NotificationPreference preference = preferenceRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreference(user));

        return convertToNotificationPreferenceResponse(preference);
    }

    @Override
    public NotificationPreferenceResponse updateNotificationPreferences(UUID userId, 
                                                                       UpdateNotificationPreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        NotificationPreference preference = preferenceRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreference(user));

        // 전체 설정 업데이트
        preference.updateGeneralSettings(
                request.getNotificationsEnabled(),
                request.getPushEnabled(),
                request.getEmailEnabled(),
                request.getSmsEnabled()
        );

        // 카테고리 설정 업데이트
        preference.updateCategorySettings(
                request.getSessionNotifications(),
                request.getSessionReminders(),
                request.getMatchingNotifications(),
                request.getChatNotifications(),
                request.getLevelTestNotifications(),
                request.getSystemNotifications(),
                request.getMarketingNotifications()
        );

        // 조용한 시간 설정 업데이트
        preference.updateQuietHours(
                request.getQuietHoursEnabled(),
                request.getQuietHoursStart(),
                request.getQuietHoursEnd(),
                request.getTimezone()
        );

        // 다이제스트 설정 업데이트
        preference.updateDigestSettings(
                request.getDigestEnabled(),
                request.getDigestFrequency(),
                request.getDigestTime()
        );

        // 언어 설정 업데이트
        preference.updateLanguage(request.getNotificationLanguage());

        NotificationPreference savedPreference = preferenceRepository.save(preference);
        return convertToNotificationPreferenceResponse(savedPreference);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats(UUID userId) {
        List<Object[]> statusStats = notificationRepository.getNotificationStatsByUserId(userId);
        List<Object[]> categoryStats = notificationRepository.getNotificationStatsByCategory(
                userId, LocalDateTime.now().minusMonths(1));

        Map<String, Long> statusBreakdown = new HashMap<>();
        long totalNotifications = 0;
        long unreadCount = 0;
        long readCount = 0;
        long sentCount = 0;
        long failedCount = 0;

        for (Object[] stat : statusStats) {
            String status = stat[0].toString();
            Long count = ((Number) stat[1]).longValue();
            statusBreakdown.put(status, count);
            totalNotifications += count;

            switch (status.toUpperCase()) {
                case "UNREAD":
                    unreadCount = count;
                    break;
                case "READ":
                    readCount = count;
                    break;
                case "SENT":
                    sentCount = count;
                    break;
                case "FAILED":
                    failedCount = count;
                    break;
            }
        }

        Map<String, Long> categoryBreakdown = categoryStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));

        // 시간별 통계
        LocalDateTime now = LocalDateTime.now();
        Long todayCount = notificationRepository.countDailyNotificationsByUserId(userId, now);
        
        return new NotificationStatsResponse(
                totalNotifications, unreadCount, readCount, sentCount, failedCount,
                statusBreakdown, categoryBreakdown, new HashMap<>(),
                0.0, todayCount, 0L, 0L
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository
                .findPendingNotificationsToSend(LocalDateTime.now());

        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notification);
            } catch (Exception e) {
                log.error("Failed to send notification: {}", notification.getNotificationId(), e);
                notification.markAsFailed("Send failed: " + e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }

    @Override
    public void sendNotificationToUser(UUID userId, NotificationType type, String title,
                                     String content, Map<String, Object> data) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(type);
        request.setTitle(title);
        request.setContent(content);
        request.setActionData(convertMapToJson(data));
        request.setPriority(2); // NORMAL

        NotificationResponse notification = createAndSendNotification(request);

        // WebSocket으로 실시간 알림 전송
        if (notification != null) {
            webSocketService.sendPersonalNotification(userId, title, content, data);
        }
    }

    @Override
    public void sendBulkNotification(List<UUID> userIds, NotificationType type, String title, 
                                   String content, Map<String, Object> data) {
        for (UUID userId : userIds) {
            try {
                sendNotificationToUser(userId, type, title, content, data);
            } catch (Exception e) {
                log.error("Failed to send bulk notification to user: {}", userId, e);
            }
        }
    }

    @Override
    public void cleanupExpiredNotifications() {
        // 만료된 알림 상태 업데이트
        int expiredCount = notificationRepository.markExpiredNotifications(LocalDateTime.now());
        log.info("Marked {} notifications as expired", expiredCount);

        // 만료된 알림 삭제 (영구보관하지 않는 것만)
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        List<Notification> toDelete = expiredNotifications.stream()
                .filter(n -> !n.getIsPersistent())
                .collect(Collectors.toList());
        
        notificationRepository.deleteAll(toDelete);
        log.info("Deleted {} expired non-persistent notifications", toDelete.size());
    }

    @Override
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository
                .findNonPersistentOldNotifications(cutoffDate);
        
        notificationRepository.deleteAll(oldNotifications);
        log.info("Deleted {} old non-persistent notifications", oldNotifications.size());
    }

    @Override
    public void registerPushToken(UUID userId, String token, String deviceType) {
        // 푸시 토큰 등록 로직 구현 (별도 엔티티 필요)
        log.debug("Push token registered for user: {}, device: {}", userId, deviceType);
    }

    @Override
    public void unregisterPushToken(UUID userId, String token) {
        // 푸시 토큰 해제 로직 구현
        log.debug("Push token unregistered for user: {}", userId);
    }

    // Private helper methods

    private boolean isDuplicateNotification(CreateNotificationRequest request) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5); // 5분 내 중복 확인
        return notificationRepository.existsSimilarRecentNotification(
                request.getUserId(), request.getType(), request.getActionData(), since);
    }

    private boolean isExceededDailyLimit(UUID userId) {
        Long dailyCount = notificationRepository.countDailyNotificationsByUserId(userId, LocalDateTime.now());
        return dailyCount >= 50; // 일일 50개 제한
    }

    private void sendNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            sendNotification(notification);
        }
    }

    private void sendNotification(Notification notification) {
        // 사용자 설정 확인
        NotificationPreference preference = preferenceRepository.findByUser(notification.getUser()).orElse(null);
        if (preference == null || !preference.shouldReceiveNotification(notification.getCategory())) {
            log.debug("Notification blocked by user preference: {}", notification.getNotificationId());
            return;
        }

        // 조용한 시간 확인
        if (preference.isInQuietHours()) {
            log.debug("Notification delayed due to quiet hours: {}", notification.getNotificationId());
            return;
        }

        // 실제 발송 로직 (푸시, 이메일, SMS)
        boolean sent = false;
        String deliveryChannels = notification.getDeliveryChannels();
        
        if (deliveryChannels != null) {
            if (deliveryChannels.contains("PUSH") && preference.getPushEnabled()) {
                sent |= sendPushNotification(notification);
            }
            if (deliveryChannels.contains("EMAIL") && preference.getEmailEnabled()) {
                sent |= sendEmailNotification(notification);
            }
            if (deliveryChannels.contains("SMS") && preference.getSmsEnabled()) {
                sent |= sendSmsNotification(notification);
            }
        }

        if (sent) {
            notification.markAsSent();
            notificationRepository.save(notification);
        }
    }

    private boolean sendPushNotification(Notification notification) {
        // 푸시 알림 발송 로직
        try {
            // Firebase FCM 등을 사용한 푸시 발송
            notification.markPushSent();
            log.debug("Push notification sent: {}", notification.getNotificationId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send push notification: {}", notification.getNotificationId(), e);
            return false;
        }
    }

    private boolean sendEmailNotification(Notification notification) {
        // 이메일 발송 로직
        try {
            notification.markEmailSent();
            log.debug("Email notification sent: {}", notification.getNotificationId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", notification.getNotificationId(), e);
            return false;
        }
    }

    private boolean sendSmsNotification(Notification notification) {
        // SMS 발송 로직
        try {
            notification.markSmsSent();
            log.debug("SMS notification sent: {}", notification.getNotificationId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", notification.getNotificationId(), e);
            return false;
        }
    }

    private CreateNotificationRequest buildNotificationFromTemplate(UUID userId, String templateId, 
                                                                   Map<String, Object> variables) {
        // 템플릿 기반 알림 생성 로직
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setTemplateId(templateId);
        request.setTemplateVariables(convertMapToJson(variables));
        
        // 템플릿에 따른 기본값 설정
        switch (templateId) {
            case "SESSION_REMINDER":
                request.setType(NotificationType.SESSION_REMINDER);
                request.setTitle("세션 알림");
                request.setContent("곧 세션이 시작됩니다.");
                request.setPriority(3);
                request.setCategory("SESSION_REMINDER");
                break;
            case "NEW_MESSAGE":
                request.setType(NotificationType.NEW_MESSAGE);
                request.setTitle("새 메시지");
                request.setContent("새로운 메시지가 도착했습니다.");
                request.setPriority(2);
                request.setCategory("CHAT");
                break;
            default:
                request.setType(NotificationType.CUSTOM);
                request.setTitle("알림");
                request.setContent("새로운 알림이 있습니다.");
                request.setPriority(1);
        }
        
        return request;
    }

    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .notificationsEnabled(true)
                .pushEnabled(true)
                .emailEnabled(true)
                .smsEnabled(false)
                .timezone("Asia/Seoul")
                .notificationLanguage("ko")
                .build();
        
        return preferenceRepository.save(preference);
    }

    private String convertMapToJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        // 실제 구현에서는 ObjectMapper 사용
        return data.toString();
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setActionUrl(notification.getActionUrl());
        response.setActionData(notification.getActionData());
        response.setImageUrl(notification.getImageUrl());
        response.setIconUrl(notification.getIconUrl());
        response.setStatus(notification.getStatus());
        response.setPriority(notification.getPriority());
        response.setCategory(notification.getCategory());
        response.setScheduledAt(notification.getScheduledAt());
        response.setSentAt(notification.getSentAt());
        response.setReadAt(notification.getReadAt());
        response.setExpiresAt(notification.getExpiresAt());
        response.setCreatedAt(notification.getCreatedAt());
        response.setIsPersistent(notification.getIsPersistent());
        response.setSenderUserId(notification.getSenderUserId());
        response.setTemplateId(notification.getTemplateId());
        response.setDeliveryChannels(notification.getDeliveryChannels());
        response.setPushSent(notification.getPushSent());
        response.setEmailSent(notification.getEmailSent());
        response.setSmsSent(notification.getSmsSent());
        response.setIsExpired(notification.isExpired());
        response.setIsHighPriority(notification.isHighPriority());
        
        return response;
    }

    private NotificationPreferenceResponse convertToNotificationPreferenceResponse(NotificationPreference preference) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setNotificationsEnabled(preference.getNotificationsEnabled());
        response.setPushEnabled(preference.getPushEnabled());
        response.setEmailEnabled(preference.getEmailEnabled());
        response.setSmsEnabled(preference.getSmsEnabled());
        response.setSessionNotifications(preference.getSessionNotifications());
        response.setSessionReminders(preference.getSessionReminders());
        response.setMatchingNotifications(preference.getMatchingNotifications());
        response.setChatNotifications(preference.getChatNotifications());
        response.setLevelTestNotifications(preference.getLevelTestNotifications());
        response.setSystemNotifications(preference.getSystemNotifications());
        response.setMarketingNotifications(preference.getMarketingNotifications());
        response.setQuietHoursEnabled(preference.getQuietHoursEnabled());
        response.setQuietHoursStart(preference.getQuietHoursStart());
        response.setQuietHoursEnd(preference.getQuietHoursEnd());
        response.setTimezone(preference.getTimezone());
        response.setNotificationLanguage(preference.getNotificationLanguage());
        response.setDigestEnabled(preference.getDigestEnabled());
        response.setDigestFrequency(preference.getDigestFrequency());
        response.setDigestTime(preference.getDigestTime());
        
        return response;
    }
}