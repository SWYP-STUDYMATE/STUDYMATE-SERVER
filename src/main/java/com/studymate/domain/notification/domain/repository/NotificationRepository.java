package com.studymate.domain.notification.domain.repository;

import com.studymate.domain.notification.entity.Notification;
import com.studymate.domain.notification.type.NotificationStatus;
import com.studymate.domain.notification.type.NotificationType;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자별 알림 조회 (페이지네이션)
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 사용자의 읽지 않은 알림 조회
    List<Notification> findByUserAndStatusOrderByCreatedAtDesc(User user, NotificationStatus status);

    // 사용자의 읽지 않은 알림 수
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") NotificationStatus status);

    // 우선순위별 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.priority >= :priority ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findByUserIdAndPriorityGreaterThanEqualOrderByPriorityDescCreatedAtDesc(
            @Param("userId") UUID userId, @Param("priority") Integer priority);

    // 카테고리별 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.category = :category ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndCategory(@Param("userId") UUID userId, 
                                              @Param("category") String category, 
                                              Pageable pageable);

    // 타입별 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndType(@Param("userId") UUID userId, @Param("type") NotificationType type);

    // 발송 예정 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) ORDER BY n.priority DESC, n.scheduledAt ASC")
    List<Notification> findPendingNotificationsToSend(@Param("now") LocalDateTime now);

    // 만료된 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    // 영구보관하지 않는 오래된 알림 조회 (정리용)
    @Query("SELECT n FROM Notification n WHERE n.isPersistent = false AND n.createdAt <= :cutoffDate")
    List<Notification> findNonPersistentOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 특정 기간의 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // 발송 채널별 미발송 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.deliveryChannels LIKE %:channel% AND " +
           "(:channel = 'PUSH' AND n.pushSent = false OR " +
           ":channel = 'EMAIL' AND n.emailSent = false OR " +
           ":channel = 'SMS' AND n.smsSent = false)")
    List<Notification> findUnsentByChannel(@Param("channel") String channel);

    // 사용자의 알림 통계
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.user.userId = :userId GROUP BY n.status")
    List<Object[]> getNotificationStatsByUserId(@Param("userId") UUID userId);

    // 카테고리별 통계
    @Query("SELECT n.category, COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.createdAt >= :since GROUP BY n.category")
    List<Object[]> getNotificationStatsByCategory(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    // 일괄 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.user.userId = :userId AND n.status = 'UNREAD'")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    // 특정 카테고리 일괄 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'read', n.readAt = :readAt WHERE n.user.userId = :userId AND n.category = :category AND n.status = 'UNREAD'")
    int markCategoryAsReadByUserId(@Param("userId") UUID userId, @Param("category") String category, @Param("readAt") LocalDateTime readAt);

    // 만료된 알림 상태 업데이트
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'EXPIRED' WHERE n.expiresAt <= :now AND n.status != 'EXPIRED'")
    int markExpiredNotifications(@Param("now") LocalDateTime now);

    // 중복 알림 확인
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.user.userId = :userId AND n.type = :type AND n.actionData = :actionData AND n.createdAt >= :since")
    Boolean existsSimilarRecentNotification(@Param("userId") UUID userId,
                                          @Param("type") NotificationType type,
                                          @Param("actionData") String actionData,
                                          @Param("since") LocalDateTime since);

    // 템플릿별 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.templateId = :templateId AND n.createdAt >= :since")
    List<Notification> findByTemplateIdAndCreatedAtAfter(@Param("templateId") String templateId,
                                                        @Param("since") LocalDateTime since);

    // 발송자별 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.senderUserId = :senderUserId ORDER BY n.createdAt DESC")
    Page<Notification> findBySenderUserId(@Param("senderUserId") String senderUserId, Pageable pageable);

    // 시간대별 알림 발송 통계
    @Query("SELECT HOUR(n.sentAt), COUNT(n) FROM Notification n WHERE n.sentAt IS NOT NULL AND DATE(n.sentAt) = DATE(:date) GROUP BY HOUR(n.sentAt)")
    List<Object[]> getHourlyNotificationStats(@Param("date") LocalDateTime date);

    // 사용자별 일일 알림 수 제한 체크
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND DATE(n.createdAt) = DATE(:date)")
    Long countDailyNotificationsByUserId(@Param("userId") UUID userId, @Param("date") LocalDateTime date);
}