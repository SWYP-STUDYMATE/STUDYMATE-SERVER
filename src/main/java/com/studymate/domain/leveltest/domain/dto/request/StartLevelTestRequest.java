package com.studymate.domain.leveltest.domain.dto.request;

import com.studymate.domain.leveltest.entity.LevelTest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartLevelTestRequest {
    
    @NotNull(message = "테스트 타입은 필수입니다")
    private LevelTest.TestType testType; // SPEAKING, LISTENING, READING, WRITING, COMPREHENSIVE, VOICE
    
    @NotBlank(message = "언어 코드는 필수입니다")
    private String languageCode; // en, ko, ja, zh, etc.
    
    private String testLevel; // BEGINNER, INTERMEDIATE, ADVANCED (optional for adaptive tests)
    
    @NotNull(message = "총 문제 수는 필수입니다")
    private Integer totalQuestions;
}