package com.studymate.domain.analytics.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_progress", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date", "language_code"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "sessions_completed", nullable = false)
    private Integer sessionsCompleted = 0;

    @Column(name = "total_session_minutes", nullable = false)
    private Integer totalSessionMinutes = 0;

    @Column(name = "messages_sent", nullable = false)
    private Integer messagesSent = 0;

    @Column(name = "words_learned", nullable = false)
    private Integer wordsLearned = 0;

    @Column(name = "tests_taken", nullable = false)
    private Integer testsTaken = 0;

    @Column(name = "average_test_score")
    private Double averageTestScore;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned = 0;

    @Column(name = "badges_earned", nullable = false)
    private Integer badgesEarned = 0;

    @Column(name = "speaking_minutes", nullable = false)
    private Integer speakingMinutes = 0;

    @Column(name = "listening_minutes", nullable = false)
    private Integer listeningMinutes = 0;

    @Column(name = "reading_minutes", nullable = false)
    private Integer readingMinutes = 0;

    @Column(name = "writing_minutes", nullable = false)
    private Integer writingMinutes = 0;

    @Builder
    public LearningProgress(User user, LocalDate date, String languageCode) {
        this.user = user;
        this.date = date;
        this.languageCode = languageCode;
        this.sessionsCompleted = 0;
        this.totalSessionMinutes = 0;
        this.messagesSent = 0;
        this.wordsLearned = 0;
        this.testsTaken = 0;
        this.streakDays = 0;
        this.xpEarned = 0;
        this.badgesEarned = 0;
        this.speakingMinutes = 0;
        this.listeningMinutes = 0;
        this.readingMinutes = 0;
        this.writingMinutes = 0;
    }

    public void addSessionCompleted(Integer sessionMinutes, String skillFocus) {
        this.sessionsCompleted++;
        this.totalSessionMinutes += sessionMinutes;
        
        // 스킬별 시간 추가
        switch (skillFocus != null ? skillFocus.toUpperCase() : "SPEAKING") {
            case "SPEAKING":
                this.speakingMinutes += sessionMinutes;
                break;
            case "LISTENING":
                this.listeningMinutes += sessionMinutes;
                break;
            case "READING":
                this.readingMinutes += sessionMinutes;
                break;
            case "WRITING":
                this.writingMinutes += sessionMinutes;
                break;
            default:
                this.speakingMinutes += sessionMinutes / 2;
                this.listeningMinutes += sessionMinutes / 2;
        }
        
        // XP 획득 (1분당 10XP)
        this.xpEarned += sessionMinutes * 10;
    }

    public void addMessageSent(Integer count) {
        this.messagesSent += count;
        this.xpEarned += count * 5; // 메시지당 5XP
    }

    public void addWordsLearned(Integer count) {
        this.wordsLearned += count;
        this.xpEarned += count * 15; // 단어당 15XP
    }

    public void addTestTaken(Double testScore) {
        this.testsTaken++;
        if (this.averageTestScore == null) {
            this.averageTestScore = testScore;
        } else {
            this.averageTestScore = (this.averageTestScore * (this.testsTaken - 1) + testScore) / this.testsTaken;
        }
        this.xpEarned += (int) (testScore * 2); // 점수 * 2 XP
    }

    public void updateStreak(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public void addBadgeEarned() {
        this.badgesEarned++;
        this.xpEarned += 100; // 배지당 100XP
    }

    public void addXP(Integer xp) {
        this.xpEarned += xp;
    }
}