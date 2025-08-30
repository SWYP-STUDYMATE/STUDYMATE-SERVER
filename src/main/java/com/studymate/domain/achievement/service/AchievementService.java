package com.studymate.domain.achievement.service;

import com.studymate.domain.achievement.domain.dto.response.AchievementResponse;
import com.studymate.domain.achievement.domain.dto.response.AchievementStatsResponse;
import com.studymate.domain.achievement.domain.dto.response.UserAchievementResponse;
import com.studymate.domain.achievement.entity.Achievement;
import com.studymate.domain.user.entity.User;

import java.util.List;

public interface AchievementService {
    
    // 모든 활성화된 성취 조회
    List<AchievementResponse> getAllActiveAchievements();
    
    // 카테고리별 성취 조회
    List<AchievementResponse> getAchievementsByCategory(Achievement.AchievementCategory category);
    
    // 사용자의 성취 현황 조회
    List<UserAchievementResponse> getUserAchievements(User user);
    
    // 사용자의 완료된 성취 조회
    List<UserAchievementResponse> getCompletedAchievements(User user);
    
    // 사용자의 진행 중인 성취 조회
    List<UserAchievementResponse> getInProgressAchievements(User user);
    
    // 사용자의 성취 통계 조회
    AchievementStatsResponse getAchievementStats(User user);
    
    // 성취 진행도 업데이트
    UserAchievementResponse updateProgress(User user, String achievementKey, Integer progress);
    
    // 성취 진행도 증가
    UserAchievementResponse incrementProgress(User user, String achievementKey, Integer increment);
    
    // 보상 수령
    UserAchievementResponse claimReward(User user, Long userAchievementId);
    
    // 사용자의 성취 초기화 (새 사용자 등록시)
    void initializeUserAchievements(User user);
    
    // 성취 달성 확인 및 처리
    List<UserAchievementResponse> checkAndCompleteAchievements(User user);
}