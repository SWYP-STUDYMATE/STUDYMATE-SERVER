package com.studymate.domain.session.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFeedbackRequest {
    
    private Integer rating; // 1-5
    private String comment;
    private Integer languageExchangeQuality; // 1-5
    private Integer communicationEffectiveness; // 1-5
    private Integer technicalQuality; // 1-5
    private Boolean recommendToOthers;
    private String improvementSuggestions;
}