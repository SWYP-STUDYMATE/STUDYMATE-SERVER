package com.studymate.domain.leveltest.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartVoiceTestRequest {
    
    private String languageCode; // "en", "ko", etc.
    private String currentLevel; // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    private String focusArea; // "PRONUNCIATION", "FLUENCY", "GRAMMAR", "VOCABULARY", "COMPREHENSIVE"
}