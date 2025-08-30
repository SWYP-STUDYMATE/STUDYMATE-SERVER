package com.studymate.domain.notification.domain.repository;

import com.studymate.domain.notification.entity.NotificationPreference;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUser(User user);

    Optional<NotificationPreference> findByUserUserId(UUID userId);

    // 푸시 알림이 활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.pushEnabled = true AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithPushEnabled();

    // 이메일 알림이 활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.emailEnabled = true AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithEmailEnabled();

    // SMS 알림이 활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.smsEnabled = true AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithSmsEnabled();

    // 특정 카테고리 알림이 활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.notificationsEnabled = true AND " +
           "(:category = 'SESSION' AND np.sessionNotifications = true OR " +
           ":category = 'SESSION_REMINDER' AND np.sessionReminders = true OR " +
           ":category = 'MATCHING' AND np.matchingNotifications = true OR " +
           ":category = 'CHAT' AND np.chatNotifications = true OR " +
           ":category = 'LEVEL_TEST' AND np.levelTestNotifications = true OR " +
           ":category = 'SYSTEM' AND np.systemNotifications = true OR " +
           ":category = 'MARKETING' AND np.marketingNotifications = true)")
    List<NotificationPreference> findUsersWithCategoryEnabled(@Param("category") String category);

    // 다이제스트가 활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.digestEnabled = true AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithDigestEnabled();

    // 특정 빈도의 다이제스트 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.digestEnabled = true AND np.digestFrequency = :frequency AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithDigestFrequency(@Param("frequency") String frequency);

    // 특정 시간대의 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.timezone = :timezone")
    List<NotificationPreference> findUsersByTimezone(@Param("timezone") String timezone);

    // 조용한 시간이 설정된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.quietHoursEnabled = true")
    List<NotificationPreference> findUsersWithQuietHoursEnabled();

    // 언어별 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.notificationLanguage = :language")
    List<NotificationPreference> findUsersByNotificationLanguage(@Param("language") String language);

    // 마케팅 알림 수신 동의 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.marketingNotifications = true AND np.notificationsEnabled = true")
    List<NotificationPreference> findUsersWithMarketingEnabled();

    // 전체 알림이 비활성화된 사용자 조회
    @Query("SELECT np FROM NotificationPreference np WHERE np.notificationsEnabled = false")
    List<NotificationPreference> findUsersWithNotificationsDisabled();

    // 사용자 설정 통계
    @Query("SELECT " +
           "SUM(CASE WHEN np.notificationsEnabled = true THEN 1 ELSE 0 END) as totalEnabled, " +
           "SUM(CASE WHEN np.pushEnabled = true THEN 1 ELSE 0 END) as pushEnabled, " +
           "SUM(CASE WHEN np.emailEnabled = true THEN 1 ELSE 0 END) as emailEnabled, " +
           "SUM(CASE WHEN np.smsEnabled = true THEN 1 ELSE 0 END) as smsEnabled, " +
           "SUM(CASE WHEN np.marketingNotifications = true THEN 1 ELSE 0 END) as marketingEnabled " +
           "FROM NotificationPreference np")
    Object[] getNotificationPreferenceStats();

    // 언어별 통계
    @Query("SELECT np.notificationLanguage, COUNT(np) FROM NotificationPreference np GROUP BY np.notificationLanguage")
    List<Object[]> getLanguageStats();

    // 시간대별 통계
    @Query("SELECT np.timezone, COUNT(np) FROM NotificationPreference np WHERE np.timezone IS NOT NULL GROUP BY np.timezone")
    List<Object[]> getTimezoneStats();
}