package com.studymate.domain.analytics.domain.repository;

import com.studymate.domain.analytics.entity.UserActivity;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    // 사용자별 활동 조회
    Page<UserActivity> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 활동 유형별 조회
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityType = :activityType AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    List<UserActivity> findByActivityTypeAndDateRange(@Param("activityType") String activityType,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // 활동 카테고리별 조회
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityCategory = :category AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    List<UserActivity> findByActivityCategoryAndDateRange(@Param("category") String category,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // 사용자의 특정 기간 활동 조회
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.userId = :userId AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    List<UserActivity> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // 일별 활성 사용자 수 계산
    @Query("SELECT COUNT(DISTINCT ua.user.userId) FROM UserActivity ua WHERE DATE(ua.createdAt) = DATE(:date)")
    Long countDistinctActiveUsersByDate(@Param("date") LocalDateTime date);

    // 활동 유형별 통계
    @Query("SELECT ua.activityType, COUNT(ua) FROM UserActivity ua WHERE ua.createdAt BETWEEN :startDate AND :endDate GROUP BY ua.activityType")
    List<Object[]> getActivityStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // 카테고리별 통계
    @Query("SELECT ua.activityCategory, COUNT(ua) FROM UserActivity ua WHERE ua.createdAt BETWEEN :startDate AND :endDate GROUP BY ua.activityCategory")
    List<Object[]> getCategoryStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // 성공/실패 통계
    @Query("SELECT ua.success, COUNT(ua) FROM UserActivity ua WHERE ua.createdAt BETWEEN :startDate AND :endDate GROUP BY ua.success")
    List<Object[]> getSuccessStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // 평균 세션 지속 시간
    @Query("SELECT AVG(ua.durationSeconds) FROM UserActivity ua WHERE ua.activityType = :activityType AND ua.durationSeconds IS NOT NULL AND ua.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageSessionDuration(@Param("activityType") String activityType,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // 시간대별 활동 통계
    @Query("SELECT HOUR(ua.createdAt), COUNT(ua) FROM UserActivity ua WHERE ua.createdAt BETWEEN :startDate AND :endDate GROUP BY HOUR(ua.createdAt) ORDER BY HOUR(ua.createdAt)")
    List<Object[]> getHourlyActivityStats(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // 사용자의 마지막 활동 시간
    @Query("SELECT MAX(ua.createdAt) FROM UserActivity ua WHERE ua.user.userId = :userId")
    LocalDateTime findLastActivityByUserId(@Param("userId") UUID userId);

    // 에러 활동 조회
    @Query("SELECT ua FROM UserActivity ua WHERE ua.success = false AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    List<UserActivity> findErrorActivitiesByDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // IP별 활동 통계 (보안 목적)
    @Query("SELECT ua.ipAddress, COUNT(ua) FROM UserActivity ua WHERE ua.createdAt BETWEEN :startDate AND :endDate GROUP BY ua.ipAddress ORDER BY COUNT(ua) DESC")
    List<Object[]> getActivityStatsByIP(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // 신규 사용자 활동
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityType = 'FIRST_LOGIN' AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    List<UserActivity> findNewUserActivities(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}