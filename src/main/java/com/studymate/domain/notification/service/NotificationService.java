package com.studymate.domain.notification.service;

import com.studymate.domain.notification.domain.dto.request.CreateNotificationRequest;
import com.studymate.domain.notification.domain.dto.request.UpdateNotificationPreferenceRequest;
import com.studymate.domain.notification.domain.dto.response.NotificationPreferenceResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationResponse;
import com.studymate.domain.notification.domain.dto.response.NotificationStatsResponse;
import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    
    // 알림 생성 및 발송
    NotificationResponse createNotification(CreateNotificationRequest request);
    
    NotificationResponse createAndSendNotification(CreateNotificationRequest request);
    
    // 템플릿 기반 알림 생성
    NotificationResponse createNotificationFromTemplate(UUID userId, String templateId, 
                                                       Map<String, Object> variables);
    
    // 알림 조회
    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);

    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable,
                                                   String category, NotificationStatus statusFilter);
    
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    
    Page<NotificationResponse> getNotificationsByCategory(UUID userId, String category, Pageable pageable);
    
    NotificationResponse getNotification(Long notificationId);
    
    // 알림 상태 관리
    void markAsRead(UUID userId, Long notificationId);
    
    void markAllAsRead(UUID userId);
    
    void markCategoryAsRead(UUID userId, String category);
    
    void deleteNotification(UUID userId, Long notificationId);
    
    void deleteAllNotifications(UUID userId);
    
    // 알림 설정 관리
    NotificationPreferenceResponse getNotificationPreferences(UUID userId);
    
    NotificationPreferenceResponse updateNotificationPreferences(UUID userId, 
                                                               UpdateNotificationPreferenceRequest request);
    
    // 알림 통계
    NotificationStatsResponse getNotificationStats(UUID userId);
    
    Long getUnreadCount(UUID userId);
    
    // 알림 발송
    void sendPendingNotifications();
    
    void sendNotificationToUser(UUID userId, NotificationType type, String title, 
                               String content, Map<String, Object> data);
    
    void sendBulkNotification(List<UUID> userIds, NotificationType type, String title, 
                             String content, Map<String, Object> data);
    
    // 시스템 관리
    void cleanupExpiredNotifications();
    
    void cleanupOldNotifications();
    
    // 푸시 토큰 관리
    void registerPushToken(UUID userId, String token, String deviceType);
    
    void unregisterPushToken(UUID userId, String token);
}
