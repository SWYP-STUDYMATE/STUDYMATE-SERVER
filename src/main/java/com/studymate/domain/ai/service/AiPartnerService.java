package com.studymate.domain.ai.service;

import com.studymate.domain.ai.domain.dto.request.SendMessageRequest;
import com.studymate.domain.ai.domain.dto.request.StartAiSessionRequest;
import com.studymate.domain.ai.domain.dto.response.AiMessageResponse;
import com.studymate.domain.ai.domain.dto.response.AiPartnerResponse;
import com.studymate.domain.ai.domain.dto.response.AiSessionResponse;
import com.studymate.domain.ai.entity.AiSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AiPartnerService {
    
    List<AiPartnerResponse> getAvailablePartners(String targetLanguage, String languageLevel);
    
    List<AiPartnerResponse> getRecommendedPartners(UUID userId);
    
    AiPartnerResponse getPartnerDetails(UUID partnerId);
    
    List<AiPartnerResponse> searchPartners(String keyword, String specialty, String personalityType);
    
    AiSessionResponse startSession(UUID userId, StartAiSessionRequest request);
    
    CompletableFuture<AiMessageResponse> sendMessage(UUID userId, SendMessageRequest request);
    
    AiSessionResponse endSession(UUID userId, UUID sessionId);
    
    AiSessionResponse pauseSession(UUID userId, UUID sessionId);
    
    AiSessionResponse resumeSession(UUID userId, UUID sessionId);
    
    AiSessionResponse getActiveSession(UUID userId);
    
    Page<AiSessionResponse> getUserSessions(UUID userId, Pageable pageable);
    
    List<AiMessageResponse> getSessionMessages(UUID userId, UUID sessionId);
    
    void rateSession(UUID userId, UUID sessionId, Integer rating, String feedback);
    
    AiSessionResponse.LearningProgress getLearningProgress(UUID userId, UUID sessionId);
    
    List<AiSessionResponse> getSessionsByType(UUID userId, AiSession.SessionType sessionType);
    
    CompletableFuture<String> generateSessionSummary(UUID sessionId);
    
    CompletableFuture<List<String>> generateImprovementSuggestions(UUID userId, UUID sessionId);
    
    List<String> getLearnedVocabulary(UUID userId, UUID sessionId);
    
    List<String> getGrammarPoints(UUID userId, UUID sessionId);
}