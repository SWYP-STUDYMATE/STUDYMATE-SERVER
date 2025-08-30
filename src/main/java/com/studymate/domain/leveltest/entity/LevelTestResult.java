package com.studymate.domain.leveltest.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "level_test_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LevelTestResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LevelTest levelTest;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "question_type", length = 50)
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, SPEAKING, LISTENING

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_audio_url", length = 500)
    private String questionAudioUrl;

    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "user_audio_url", length = 500)
    private String userAudioUrl; // For speaking tests

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "max_points")
    private Integer maxPoints;

    @Column(name = "response_time_seconds")
    private Integer responseTimeSeconds;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel; // EASY, MEDIUM, HARD

    @Column(name = "skill_category", length = 50)
    private String skillCategory; // GRAMMAR, VOCABULARY, COMPREHENSION, PRONUNCIATION

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Builder
    public LevelTestResult(LevelTest levelTest, Integer questionNumber, String questionType,
                          String questionText, String questionAudioUrl, String questionImageUrl,
                          String correctAnswer, String difficultyLevel, String skillCategory,
                          Integer maxPoints, String explanation) {
        this.levelTest = levelTest;
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.questionText = questionText;
        this.questionAudioUrl = questionAudioUrl;
        this.questionImageUrl = questionImageUrl;
        this.correctAnswer = correctAnswer;
        this.difficultyLevel = difficultyLevel;
        this.skillCategory = skillCategory;
        this.maxPoints = maxPoints;
        this.explanation = explanation;
        this.isCorrect = false;
        this.pointsEarned = 0;
    }

    public void submitAnswer(String userAnswer, String userAudioUrl, Integer responseTimeSeconds) {
        this.userAnswer = userAnswer;
        this.userAudioUrl = userAudioUrl;
        this.responseTimeSeconds = responseTimeSeconds;
        
        // 답안 정확성 확인
        this.isCorrect = checkAnswer(userAnswer);
        this.pointsEarned = this.isCorrect ? this.maxPoints : 0;
    }

    private Boolean checkAnswer(String userAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        
        // 기본적인 문자열 비교 (대소문자 무시, 공백 정리)
        String normalizedUser = userAnswer.trim().toLowerCase();
        String normalizedCorrect = correctAnswer.trim().toLowerCase();
        
        return normalizedUser.equals(normalizedCorrect);
    }

    public void updateGradingResult(Boolean isCorrect, Integer pointsEarned) {
        this.isCorrect = isCorrect;
        this.pointsEarned = pointsEarned;
    }
}