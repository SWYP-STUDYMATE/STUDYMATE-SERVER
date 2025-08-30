package com.studymate.domain.achievement.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_achievement_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    // 현재 진행도
    @Column(name = "current_progress")
    @Builder.Default
    private Integer currentProgress = 0;
    
    // 달성 여부
    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;
    
    // 달성 일시
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // 보상 수령 여부
    @Column(name = "is_reward_claimed")
    @Builder.Default
    private Boolean isRewardClaimed = false;
    
    // 보상 수령 일시
    @Column(name = "reward_claimed_at")
    private LocalDateTime rewardClaimedAt;
    
    // 진행도 업데이트
    public void updateProgress(Integer progress) {
        this.currentProgress = progress;
        
        // 목표 달성 확인
        if (!this.isCompleted && this.achievement.getTargetValue() != null 
            && this.currentProgress >= this.achievement.getTargetValue()) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    // 진행도 증가
    public void incrementProgress(Integer increment) {
        updateProgress(this.currentProgress + increment);
    }
    
    // 보상 수령 처리
    public void claimReward() {
        if (this.isCompleted && !this.isRewardClaimed) {
            this.isRewardClaimed = true;
            this.rewardClaimedAt = LocalDateTime.now();
        }
    }
    
    // 진행률 계산 (백분율)
    public Double getProgressPercentage() {
        if (achievement.getTargetValue() == null || achievement.getTargetValue() == 0) {
            return isCompleted ? 100.0 : 0.0;
        }
        
        double percentage = (double) currentProgress / achievement.getTargetValue() * 100;
        return Math.min(percentage, 100.0);
    }
    
    // 복합 인덱스를 위한 유니크 제약조건
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
    })
    public static class UserAchievementConstraints {}
}