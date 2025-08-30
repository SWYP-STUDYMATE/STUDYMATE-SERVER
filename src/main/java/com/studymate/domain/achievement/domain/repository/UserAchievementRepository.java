package com.studymate.domain.achievement.domain.repository;

import com.studymate.domain.achievement.entity.Achievement;
import com.studymate.domain.achievement.entity.UserAchievement;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    // 사용자별 성취 조회
    List<UserAchievement> findByUserOrderByCompletedAtDesc(User user);
    
    // 사용자별 완료된 성취 조회
    List<UserAchievement> findByUserAndIsCompletedTrueOrderByCompletedAtDesc(User user);
    
    // 사용자별 진행 중인 성취 조회
    List<UserAchievement> findByUserAndIsCompletedFalseOrderByCurrentProgressDesc(User user);
    
    // 사용자별 특정 성취 조회
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
    
    // 사용자별 보상 미수령 성취 조회
    List<UserAchievement> findByUserAndIsCompletedTrueAndIsRewardClaimedFalseOrderByCompletedAtAsc(User user);
    
    // 사용자별 성취 완료 개수
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = true")
    Long countCompletedAchievementsByUser(@Param("user") User user);
    
    // 사용자별 진행 중인 성취 개수
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = false AND ua.currentProgress > 0")
    Long countInProgressAchievementsByUser(@Param("user") User user);
    
    // 사용자별 총 획득 XP
    @Query("SELECT COALESCE(SUM(ua.achievement.xpReward), 0) FROM UserAchievement ua WHERE ua.user = :user AND ua.isRewardClaimed = true")
    Long getTotalXpByUser(@Param("user") User user);
    
    // 사용자별 카테고리별 완료 성취 개수
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = true AND ua.achievement.category = :category")
    Long countCompletedAchievementsByUserAndCategory(@Param("user") User user, @Param("category") Achievement.AchievementCategory category);
    
    // 사용자별 티어별 완료 성취 개수
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = true AND ua.achievement.tier = :tier")
    Long countCompletedAchievementsByUserAndTier(@Param("user") User user, @Param("tier") Achievement.AchievementTier tier);
    
    // 특정 기간 내 완료된 성취 조회
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = true AND ua.completedAt BETWEEN :startDate AND :endDate ORDER BY ua.completedAt DESC")
    List<UserAchievement> findCompletedAchievementsByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 사용자의 성취 진행도가 특정 값 이상인 성취들
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.currentProgress >= :minProgress ORDER BY ua.currentProgress DESC")
    List<UserAchievement> findByUserAndProgressGreaterThanEqual(@Param("user") User user, @Param("minProgress") Integer minProgress);
    
    // 달성 직전 성취들 (진행도 80% 이상)
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = false AND ua.achievement.targetValue IS NOT NULL AND (ua.currentProgress * 100.0 / ua.achievement.targetValue) >= 80 ORDER BY (ua.currentProgress * 100.0 / ua.achievement.targetValue) DESC")
    List<UserAchievement> findNearCompletionAchievements(@Param("user") User user);
    
    // 최근 완료된 성취 조회 (상위 N개)
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.isCompleted = true ORDER BY ua.completedAt DESC LIMIT :limit")
    List<UserAchievement> findRecentCompletedAchievements(@Param("user") User user, @Param("limit") int limit);
}