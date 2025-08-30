package com.studymate.domain.achievement.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AchievementStatsResponse {
    private Long totalAchievements;
    private Long completedAchievements; 
    private Long inProgressAchievements;
    private Long totalXpEarned;
    private Long unclaimedRewards;
    private Double completionRate;
    private Map<String, Long> achievementsByCategory;
    private Map<String, Long> achievementsByTier;
    private List<UserAchievementResponse> recentCompletions;
    private List<UserAchievementResponse> nearCompletion;
}