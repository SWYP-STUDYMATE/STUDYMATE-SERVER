package com.studymate.domain.ai.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ai_partners")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPartner extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "target_language", length = 10, nullable = false)
    private String targetLanguage;
    
    @Column(name = "language_level", length = 20, nullable = false)
    private String languageLevel;
    
    @Column(name = "personality_type", length = 50)
    private String personalityType;
    
    @Column(name = "specialty", length = 100)
    private String specialty;
    
    @Column(name = "avatar_image", length = 200)
    private String avatarImage;
    
    @Column(name = "voice_type", length = 50)
    private String voiceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_model")
    private AiModel aiModel;
    
    @Column(name = "system_prompt", length = 2000)
    private String systemPrompt;
    
    @Column(name = "greeting_message", length = 500)
    private String greetingMessage;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "rating_average")
    private Double ratingAverage;
    
    @Column(name = "rating_count")
    private Integer ratingCount;
    
    @Column(name = "session_count")
    private Integer sessionCount;
    
    public enum AiModel {
        GPT_4,
        GPT_3_5_TURBO,
        CLAUDE_3,
        GEMINI_PRO
    }
}