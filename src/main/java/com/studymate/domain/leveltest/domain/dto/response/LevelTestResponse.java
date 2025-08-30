package com.studymate.domain.leveltest.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelTestResponse {
    private Long testId;
    private String testType;
    private String languageCode;
    private String testLevel;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double accuracyPercentage;
    private String estimatedLevel;
    private Integer estimatedScore;
    private Integer testDurationSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean isCompleted;
    private String feedback;
    private String strengths;
    private String weaknesses;
    private String recommendations;
    private List<LevelTestResultResponse> results;
}