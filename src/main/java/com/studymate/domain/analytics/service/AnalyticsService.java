package com.studymate.domain.analytics.service;

import com.studymate.domain.analytics.domain.dto.response.SystemAnalyticsResponse;
import com.studymate.domain.analytics.domain.dto.response.UserStatsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AnalyticsService {
    // 사용자 통계
    UserStatsResponse getUserStats(UUID userId);
    
    UserStatsResponse getUserStatsByDateRange(UUID userId, LocalDate startDate, LocalDate endDate);
    
    // 시스템 통계
    SystemAnalyticsResponse getSystemAnalytics();
    
    SystemAnalyticsResponse getSystemAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // 활동 기록
    void recordUserActivity(UUID userId, String activityType, String activityCategory, 
                           String description, String metadata, String ipAddress, String userAgent);
    
    // 학습 진도 업데이트
    void updateLearningProgress(UUID userId, String languageCode, String progressType, 
                               Integer value, String metadata);
    
    // 시스템 메트릭 기록
    void recordSystemMetric(String metricName, String metricCategory, Double metricValue, 
                           String metricUnit, String aggregationPeriod);
    
    // 배치 통계 계산
    void calculateDailyMetrics(LocalDate date);
    
    void calculateWeeklyMetrics(LocalDate weekStart);
    
    void calculateMonthlyMetrics(LocalDate monthStart);
}