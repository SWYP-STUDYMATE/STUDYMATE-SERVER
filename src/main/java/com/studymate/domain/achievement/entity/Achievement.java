package com.studymate.domain.achievement.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String achievementKey;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementTier tier;
    
    // 달성 조건
    @Column(name = "target_value")
    private Integer targetValue;
    
    @Column(name = "target_unit", length = 50)
    private String targetUnit;
    
    // 보상
    @Column(name = "xp_reward")
    private Integer xpReward;
    
    @Column(name = "badge_icon_url", length = 500)
    private String badgeIconUrl;
    
    @Column(name = "badge_color", length = 10)
    private String badgeColor;
    
    // 활성화 여부
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // 숨김 여부 (달성 전까지 숨김)
    @Builder.Default
    @Column(name = "is_hidden")
    private Boolean isHidden = false;
    
    // 순서
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    // 선행 조건 (다른 성취 필요)
    @Column(name = "prerequisite_achievement_id")
    private Long prerequisiteAchievementId;
    
    public enum AchievementCategory {
        LEARNING,       // 학습 관련
        SOCIAL,         // 소셜 활동
        ENGAGEMENT,     // 참여도
        SKILL,          // 스킬 발전
        TIME,           // 시간 기반
        MILESTONE,      // 마일스톤
        SPECIAL         // 특별 성취
    }
    
    public enum AchievementType {
        COUNT,          // 횟수 기반 (세션 10회)
        STREAK,         // 연속 기간 (7일 연속)
        ACCUMULATE,     // 누적 기반 (총 1000점)
        THRESHOLD,      // 임계값 기반 (레벨 5 달성)
        MILESTONE,      // 특정 날짜/이벤트
        COMBINATION     // 복합 조건
    }
    
    public enum AchievementTier {
        BRONZE,         // 브론즈 (기본)
        SILVER,         // 실버 (중급)  
        GOLD,           // 골드 (고급)
        PLATINUM,       // 플래티넘 (전문)
        DIAMOND,        // 다이아몬드 (마스터)
        LEGENDARY       // 전설 (특별)
    }
}