package com.studymate.domain.leveltest.service;

import com.studymate.domain.leveltest.domain.dto.request.StartLevelTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.SubmitAnswerRequest;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestResponse;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface LevelTestService {
    LevelTestResponse startLevelTest(UUID userId, StartLevelTestRequest request);
    
    LevelTestResponse submitAnswer(UUID userId, SubmitAnswerRequest request);
    
    LevelTestResponse completeLevelTest(UUID userId, Long testId);
    
    LevelTestResponse getLevelTest(UUID userId, Long testId);
    
    Page<LevelTestResponse> getUserLevelTests(UUID userId, Pageable pageable);
    
    LevelTestSummaryResponse getUserLevelTestSummary(UUID userId);
    
    // 음성 테스트 관련 메서드
    LevelTestResponse startVoiceLevelTest(UUID userId, String languageCode);
    
    LevelTestResponse uploadVoiceRecording(UUID userId, Long testId, MultipartFile audioFile);
    
    LevelTestResponse processVoiceTest(UUID userId, Long testId);
    
    String generateVoiceTestPrompt(String level, String language);
}