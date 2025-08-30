package com.studymate.domain.analytics.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private Integer totalXP;
    private Integer currentStreak;
    private Integer totalStudyTimeMinutes;
    private Integer completedSessions;
    private Integer totalMessages;
    private Integer wordsLearned;
    private Integer testsCompleted;
    private Double averageTestScore;
    private Integer badgesEarned;
    private Map<String, Integer> languageProgress; // language -> XP
    private Map<String, Integer> skillProgress; // skill -> minutes
    private List<DailyProgress> recentActivity;
    private List<Achievement> achievements;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyProgress {
        private LocalDate date;
        private Integer xpEarned;
        private Integer sessionMinutes;
        private Integer messagesCount;
        private Boolean hasActivity;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Achievement {
        private String badgeName;
        private String description;
        private LocalDate earnedDate;
        private String iconUrl;
    }
}