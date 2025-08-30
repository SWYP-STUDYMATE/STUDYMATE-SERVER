package com.studymate.domain.achievement.domain.dto.response;

import com.studymate.domain.achievement.entity.Achievement;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AchievementResponse {
    private Long id;
    private String achievementKey;
    private String title;
    private String description;
    private String category;
    private String type;
    private String tier;
    private Integer targetValue;
    private String targetUnit;
    private Integer xpReward;
    private String badgeIconUrl;
    private String badgeColor;
    private Boolean isActive;
    private Boolean isHidden;
    private Integer sortOrder;
    private Long prerequisiteAchievementId;
    
    public static AchievementResponse from(Achievement achievement) {
        return AchievementResponse.builder()
            .id(achievement.getId())
            .achievementKey(achievement.getAchievementKey())
            .title(achievement.getTitle())
            .description(achievement.getDescription())
            .category(achievement.getCategory().name())
            .type(achievement.getType().name())
            .tier(achievement.getTier().name())
            .targetValue(achievement.getTargetValue())
            .targetUnit(achievement.getTargetUnit())
            .xpReward(achievement.getXpReward())
            .badgeIconUrl(achievement.getBadgeIconUrl())
            .badgeColor(achievement.getBadgeColor())
            .isActive(achievement.getIsActive())
            .isHidden(achievement.getIsHidden())
            .sortOrder(achievement.getSortOrder())
            .prerequisiteAchievementId(achievement.getPrerequisiteAchievementId())
            .build();
    }
}