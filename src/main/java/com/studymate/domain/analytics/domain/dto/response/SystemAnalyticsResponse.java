package com.studymate.domain.analytics.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemAnalyticsResponse {
    private Long totalUsers;
    private Long activeUsersToday;
    private Long activeUsersThisWeek;
    private Long activeUsersThisMonth;
    private Long totalSessions;
    private Long completedSessionsToday;
    private Double averageSessionDuration;
    private Long totalMessages;
    private Long messagesThisWeek;
    private Map<String, Long> usersByLanguage;
    private Map<String, Long> sessionsByType;
    private List<TrendData> userGrowthTrend;
    private List<TrendData> activityTrend;
    private List<TrendData> engagementTrend;
    private Map<Integer, Long> activityByHour; // hour -> count
    private List<TopLanguage> topLanguages;
    private SystemHealthMetrics systemHealth;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendData {
        private LocalDateTime date;
        private Double value;
        private String label;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopLanguage {
        private String languageCode;
        private String languageName;
        private Long learnerCount;
        private Long totalSessions;
        private Double averageProgress;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemHealthMetrics {
        private Double successRate;
        private Double averageResponseTime;
        private Long errorCount;
        private String systemStatus; // HEALTHY, WARNING, CRITICAL
    }
}