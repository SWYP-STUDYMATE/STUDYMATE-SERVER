package com.studymate.domain.leveltest.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelTestSummaryResponse {
    private List<String> testedLanguages;
    private Long totalCompletedTests;
    private Map<String, String> latestLevels; // language -> level
    private Map<String, Double> averageAccuracies; // language -> accuracy
    private List<RecentTestSummary> recentTests;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentTestSummary {
        private Long testId;
        private String testType;
        private String languageCode;
        private String estimatedLevel;
        private Double accuracyPercentage;
        private LocalDateTime completedAt;
    }
}