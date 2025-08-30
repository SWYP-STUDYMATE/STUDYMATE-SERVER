package com.studymate.domain.leveltest.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "level_tests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LevelTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "test_type", nullable = false, length = 50)
    private String testType; // SPEAKING, LISTENING, READING, WRITING, COMPREHENSIVE

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode; // en, ko, ja, zh, etc.

    @Column(name = "test_level", length = 20)
    private String testLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "accuracy_percentage", nullable = false)
    private Double accuracyPercentage;

    @Column(name = "estimated_level", length = 20)
    private String estimatedLevel; // A1, A2, B1, B2, C1, C2

    @Column(name = "estimated_score")
    private Integer estimatedScore; // 0-100

    @Column(name = "test_duration_seconds")
    private Integer testDurationSeconds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // 음성 테스트 관련 필드 추가
    @Column(name = "audio_file_url")
    private String audioFileUrl;

    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    @Column(name = "pronunciation_score")
    private Integer pronunciationScore; // 0-100

    @Column(name = "fluency_score")
    private Integer fluencyScore; // 0-100

    @Column(name = "grammar_score")
    private Integer grammarScore; // 0-100

    @Column(name = "vocabulary_score")
    private Integer vocabularyScore; // 0-100

    @Column(name = "is_voice_test", nullable = false)
    private Boolean isVoiceTest = false;

    @Column(name = "voice_analysis_result", columnDefinition = "JSON")
    private String voiceAnalysisResult;

    @OneToMany(mappedBy = "levelTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LevelTestResult> testResults;

    @Builder
    public LevelTest(User user, String testType, String languageCode, String testLevel,
                    Integer totalQuestions, LocalDateTime startedAt) {
        this.user = user;
        this.testType = testType;
        this.languageCode = languageCode;
        this.testLevel = testLevel;
        this.totalQuestions = totalQuestions;
        this.startedAt = startedAt;
        this.correctAnswers = 0;
        this.accuracyPercentage = 0.0;
        this.isCompleted = false;
    }

    public void completeTest(Integer correctAnswers, Double accuracyPercentage,
                           String estimatedLevel, Integer estimatedScore,
                           Integer testDurationSeconds, String feedback,
                           String strengths, String weaknesses, String recommendations) {
        this.correctAnswers = correctAnswers;
        this.accuracyPercentage = accuracyPercentage;
        this.estimatedLevel = estimatedLevel;
        this.estimatedScore = estimatedScore;
        this.testDurationSeconds = testDurationSeconds;
        this.completedAt = LocalDateTime.now();
        this.isCompleted = true;
        this.feedback = feedback;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.recommendations = recommendations;
    }

    public void updateProgress(Integer correctAnswers, Double accuracyPercentage) {
        this.correctAnswers = correctAnswers;
        this.accuracyPercentage = accuracyPercentage;
    }

    // 음성 테스트 관련 메서드 추가
    public void setAsVoiceTest() {
        this.isVoiceTest = true;
    }

    public void updateAudioFile(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public void updateTranscript(String transcriptText) {
        this.transcriptText = transcriptText;
    }

    public void updateVoiceScores(Integer pronunciation, Integer fluency, Integer grammar, Integer vocabulary) {
        this.pronunciationScore = pronunciation;
        this.fluencyScore = fluency;
        this.grammarScore = grammar;
        this.vocabularyScore = vocabulary;
        
        // 전체 점수를 음성 테스트 기준으로 재계산
        if (pronunciation != null && fluency != null && grammar != null && vocabulary != null) {
            this.estimatedScore = (int) Math.round(
                pronunciation * 0.35 + fluency * 0.25 + grammar * 0.25 + vocabulary * 0.15
            );
        }
    }

    public void updateVoiceAnalysisResult(String analysisResult) {
        this.voiceAnalysisResult = analysisResult;
    }

    public void completeVoiceTest(String transcriptText, Integer pronunciation, Integer fluency, 
                                  Integer grammar, Integer vocabulary, Integer testDurationSeconds,
                                  String feedback, String strengths, String weaknesses, String recommendations) {
        this.transcriptText = transcriptText;
        updateVoiceScores(pronunciation, fluency, grammar, vocabulary);
        this.testDurationSeconds = testDurationSeconds;
        this.completedAt = LocalDateTime.now();
        this.isCompleted = true;
        this.feedback = feedback;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.recommendations = recommendations;
        
        // CEFR 레벨 자동 계산
        this.estimatedLevel = calculateCefrLevel(this.estimatedScore);
    }

    private String calculateCefrLevel(Integer score) {
        if (score == null) return "A1";
        
        if (score >= 95) return "C2";
        else if (score >= 85) return "C1";
        else if (score >= 75) return "B2";
        else if (score >= 65) return "B1";
        else if (score >= 45) return "A2";
        else return "A1";
    }

    public boolean isVoiceTest() {
        return Boolean.TRUE.equals(this.isVoiceTest);
    }
}