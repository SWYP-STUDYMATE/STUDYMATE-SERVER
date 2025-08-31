package com.studymate.domain.ai.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSession extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "ai_partner_id", nullable = false)
    private UUID aiPartnerId;
    
    @Column(name = "session_title", length = 200)
    private String sessionTitle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type")
    private SessionType sessionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SessionStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "message_count")
    private Integer messageCount;
    
    @Column(name = "user_rating")
    private Integer userRating;
    
    @Column(name = "user_feedback", length = 1000)
    private String userFeedback;
    
    @Column(name = "learning_objectives", length = 500)
    private String learningObjectives;
    
    @Column(name = "session_summary", length = 1000)
    private String sessionSummary;
    
    @Column(name = "improvement_suggestions", length = 1000)
    private String improvementSuggestions;
    
    @Column(name = "vocabulary_learned", length = 500)
    private String vocabularyLearned;
    
    @Column(name = "grammar_points", length = 500)
    private String grammarPoints;
    
    public enum SessionType {
        CONVERSATION,
        GRAMMAR_PRACTICE,
        VOCABULARY_BUILDING,
        PRONUNCIATION_TRAINING,
        WRITING_PRACTICE,
        LISTENING_COMPREHENSION,
        INTERVIEW_SIMULATION,
        FREE_TALK
    }
    
    public enum SessionStatus {
        ACTIVE,
        COMPLETED,
        PAUSED,
        CANCELLED
    }
}