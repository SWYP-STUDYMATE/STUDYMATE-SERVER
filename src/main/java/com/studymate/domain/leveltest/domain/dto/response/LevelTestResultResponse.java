package com.studymate.domain.leveltest.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelTestResultResponse {
    private Long resultId;
    private Integer questionNumber;
    private String questionType;
    private String questionText;
    private String questionAudioUrl;
    private String questionImageUrl;
    private String correctAnswer;
    private String userAnswer;
    private String userAudioUrl;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private Integer maxPoints;
    private Integer responseTimeSeconds;
    private String difficultyLevel;
    private String skillCategory;
    private String explanation;
}