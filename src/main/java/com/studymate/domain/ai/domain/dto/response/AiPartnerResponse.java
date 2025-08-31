package com.studymate.domain.ai.domain.dto.response;

import com.studymate.domain.ai.entity.AiPartner;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AiPartnerResponse {
    
    private UUID id;
    private String name;
    private String description;
    private String targetLanguage;
    private String languageLevel;
    private String personalityType;
    private String specialty;
    private String avatarImage;
    private String voiceType;
    private AiPartner.AiModel aiModel;
    private String greetingMessage;
    private Double ratingAverage;
    private Integer ratingCount;
    private Integer sessionCount;
    private Boolean isActive;
    private Boolean isRecommended;
}