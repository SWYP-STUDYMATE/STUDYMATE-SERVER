package com.studymate.domain.achievement.domain.repository;

import com.studymate.domain.achievement.entity.Achievement;
import com.studymate.domain.achievement.entity.Achievement.AchievementCategory;
import com.studymate.domain.achievement.entity.Achievement.AchievementTier;
import com.studymate.domain.achievement.entity.Achievement.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    // 활성화된 성취만 조회
    List<Achievement> findByIsActiveTrue();
    
    // 카테고리별 활성화된 성취 조회
    List<Achievement> findByCategoryAndIsActiveTrueOrderBySortOrderAsc(AchievementCategory category);
    
    // 티어별 활성화된 성취 조회
    List<Achievement> findByTierAndIsActiveTrueOrderBySortOrderAsc(AchievementTier tier);
    
    // 타입별 활성화된 성취 조회
    List<Achievement> findByTypeAndIsActiveTrueOrderBySortOrderAsc(AchievementType type);
    
    // 성취 키로 조회
    Optional<Achievement> findByAchievementKeyAndIsActiveTrue(String achievementKey);
    
    // 숨김이 아닌 활성화된 성취 조회
    List<Achievement> findByIsActiveTrueAndIsHiddenFalseOrderBySortOrderAsc();
    
    // 선행 조건 없는 성취 조회
    List<Achievement> findByPrerequisiteAchievementIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    
    // 특정 성취를 선행 조건으로 하는 성취들 조회
    List<Achievement> findByPrerequisiteAchievementIdAndIsActiveTrueOrderBySortOrderAsc(Long prerequisiteId);
    
    // 카테고리와 타입으로 활성화된 성취 조회
    @Query("SELECT a FROM Achievement a WHERE a.category = :category AND a.type = :type AND a.isActive = true ORDER BY a.sortOrder ASC")
    List<Achievement> findByCategoryAndType(@Param("category") AchievementCategory category, @Param("type") AchievementType type);
    
    // 목표값 범위로 성취 조회
    @Query("SELECT a FROM Achievement a WHERE a.targetValue BETWEEN :minValue AND :maxValue AND a.isActive = true ORDER BY a.targetValue ASC")
    List<Achievement> findByTargetValueBetween(@Param("minValue") Integer minValue, @Param("maxValue") Integer maxValue);
    
    // XP 보상 범위로 성취 조회
    @Query("SELECT a FROM Achievement a WHERE a.xpReward BETWEEN :minXp AND :maxXp AND a.isActive = true ORDER BY a.xpReward DESC")
    List<Achievement> findByXpRewardBetween(@Param("minXp") Integer minXp, @Param("maxXp") Integer maxXp);
    
    // 최대 정렬 순서 조회 (새 성취 추가시 사용)
    @Query("SELECT COALESCE(MAX(a.sortOrder), 0) FROM Achievement a WHERE a.category = :category")
    Integer findMaxSortOrderByCategory(@Param("category") AchievementCategory category);
}