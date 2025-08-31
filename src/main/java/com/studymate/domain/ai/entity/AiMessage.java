package com.studymate.domain.ai.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ai_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type")
    private SenderType senderType;
    
    @Column(name = "message_content", length = 2000, nullable = false)
    private String messageContent;
    
    @Column(name = "original_text", length = 2000)
    private String originalText;
    
    @Column(name = "corrected_text", length = 2000)
    private String correctedText;
    
    @Column(name = "corrections_json", length = 1000)
    private String correctionsJson;
    
    @Column(name = "grammar_feedback", length = 500)
    private String grammarFeedback;
    
    @Column(name = "vocabulary_suggestions", length = 500)
    private String vocabularySuggestions;
    
    @Column(name = "pronunciation_feedback", length = 500)
    private String pronunciationFeedback;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;
    
    public enum SenderType {
        USER,
        AI_PARTNER
    }
    
    public enum MessageType {
        TEXT,
        CORRECTION,
        FEEDBACK,
        SUGGESTION,
        SYSTEM,
        GREETING
    }
}