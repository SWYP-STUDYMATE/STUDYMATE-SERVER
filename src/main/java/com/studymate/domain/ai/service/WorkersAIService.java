package com.studymate.domain.ai.service;

import com.studymate.domain.leveltest.domain.dto.response.VoiceAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface WorkersAIService {
    
    /**
     * 음성 파일을 텍스트로 변환
     */
    String transcribeAudio(MultipartFile audioFile);
    
    /**
     * 레벨 테스트 평가
     */
    VoiceAnalysisResponse evaluateLevelTest(String transcript, String language, Map<String, Object> questions);
    
    /**
     * 실시간 대화 피드백 생성
     */
    Map<String, Object> generateRealtimeFeedback(String transcript, String context, String userLevel);
    
    /**
     * 학습 추천 생성
     */
    Map<String, Object> generateLearningRecommendations(String userLevel, Map<String, Object> weaknesses);
}