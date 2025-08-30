package com.studymate.domain.achievement.domain.dto.response;

import com.studymate.domain.achievement.entity.UserAchievement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserAchievementResponse {
    private Long id;
    private AchievementResponse achievement;
    private Integer currentProgress;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Boolean isRewardClaimed;
    private LocalDateTime rewardClaimedAt;
    private Double progressPercentage;
    
    public static UserAchievementResponse from(UserAchievement userAchievement) {
        return UserAchievementResponse.builder()
            .id(userAchievement.getId())
            .achievement(AchievementResponse.from(userAchievement.getAchievement()))
            .currentProgress(userAchievement.getCurrentProgress())
            .isCompleted(userAchievement.getIsCompleted())
            .completedAt(userAchievement.getCompletedAt())
            .isRewardClaimed(userAchievement.getIsRewardClaimed())
            .rewardClaimedAt(userAchievement.getRewardClaimedAt())
            .progressPercentage(userAchievement.getProgressPercentage())
            .build();
    }
}