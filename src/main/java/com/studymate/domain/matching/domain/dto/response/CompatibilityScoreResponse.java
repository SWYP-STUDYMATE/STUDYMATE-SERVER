package com.studymate.domain.matching.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompatibilityScoreResponse {
    private double overallScore;
    private Map<String, Double> categoryScores;
    private String compatibilityLevel; // HIGH, MEDIUM, LOW
    private String recommendation;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryScore {
        private String category;
        private double score;
        private String description;
    }
}