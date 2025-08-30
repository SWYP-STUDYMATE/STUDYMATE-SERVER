package com.studymate.domain.leveltest.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceTestPromptResponse {
    
    private Long testId;
    private String testType;
    private String languageCode;
    private String currentLevel;
    
    private List<VoicePrompt> prompts;
    private String instructions;
    private Integer estimatedDurationMinutes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoicePrompt {
        private String promptId;
        private String promptText;
        private String promptType; // "READ_ALOUD", "DESCRIBE_IMAGE", "ANSWER_QUESTION", "FREE_SPEAKING"
        private String difficulty; // "EASY", "MEDIUM", "HARD"
        private Integer timeLimit; // seconds
        private String instructions;
        private String imageUrl; // for image description prompts
        private String audioUrl; // for listening-based prompts
    }
}