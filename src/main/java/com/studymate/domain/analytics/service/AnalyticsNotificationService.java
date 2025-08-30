package com.studymate.domain.analytics.service;

import com.studymate.domain.analytics.domain.dto.response.SystemAnalyticsResponse;
import com.studymate.domain.analytics.domain.dto.response.UserStatsResponse;

import java.util.UUID;

public interface AnalyticsNotificationService {
    
    /**
     * 사용자에게 실시간 통계 업데이트 알림 전송
     */
    void notifyUserStatsUpdate(UUID userId, UserStatsResponse stats);
    
    /**
     * 관리자에게 시스템 통계 업데이트 알림 전송
     */
    void notifySystemAnalyticsUpdate(SystemAnalyticsResponse analytics);
    
    /**
     * 특정 활동에 대한 실시간 피드백 전송 (XP 획득, 레벨업 등)
     */
    void notifyActivityFeedback(UUID userId, String activityType, String message, Object data);
    
    /**
     * 시스템 경고 알림 (에러율 증가, 성능 저하 등)
     */
    void notifySystemAlert(String alertType, String message, String severity);
    
    /**
     * 학습 목표 달성 알림
     */
    void notifyGoalAchievement(UUID userId, String goalType, String achievement);
}