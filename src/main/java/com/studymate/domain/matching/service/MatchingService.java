package com.studymate.domain.matching.service;

import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.matching.domain.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MatchingService {
    Page<RecommendedPartnerResponse> getRecommendedPartners(UUID userId, Pageable pageable, 
                                                           String nativeLanguage, String targetLanguage, 
                                                           String languageLevel, Integer minAge, Integer maxAge);
    
    /**
     * 고급 필터를 사용한 파트너 추천
     */
    Page<RecommendedPartnerResponse> getRecommendedPartnersAdvanced(UUID userId, 
                                                                   AdvancedMatchingFilterRequest filters, 
                                                                   Pageable pageable);
    
    /**
     * 온라인 사용자 중에서 파트너 추천
     */
    Page<RecommendedPartnerResponse> getOnlinePartners(UUID userId, 
                                                      AdvancedMatchingFilterRequest filters, 
                                                      Pageable pageable);
    
    void sendMatchingRequest(UUID senderId, UUID targetUserId, String message);
    
    Page<SentMatchingRequestResponse> getSentMatchingRequests(UUID userId, Pageable pageable);
    
    Page<ReceivedMatchingRequestResponse> getReceivedMatchingRequests(UUID userId, Pageable pageable);
    
    void acceptMatchingRequest(UUID userId, UUID requestId);
    
    void rejectMatchingRequest(UUID userId, UUID requestId);
    
    Page<MatchedPartnerResponse> getMatchedPartners(UUID userId, Pageable pageable);
    
    void removeMatch(UUID userId, UUID matchId);
    
    CompatibilityScoreResponse getCompatibilityScore(UUID userId, UUID targetUserId);
    
    // 스마트 매칭 및 최적화 기능
    /**
     * AI 기반 스마트 매칭 - 사용자 행동 패턴과 선호도를 학습하여 최적화된 파트너 추천
     */
    Page<RecommendedPartnerResponse> getSmartRecommendations(UUID userId, Pageable pageable);
    
    /**
     * 실시간 매칭 - 현재 온라인인 사용자들 중에서 즉시 매칭 가능한 파트너 찾기
     */
    Page<RecommendedPartnerResponse> getRealTimeMatches(UUID userId, String sessionType);
    
    /**
     * 매칭 품질 피드백 수집
     */
    void recordMatchingFeedback(UUID userId, UUID partnerId, int qualityScore, String feedback);
    
    /**
     * 사용자 매칭 선호도 업데이트
     */
    void updateMatchingPreferences(UUID userId, AdvancedMatchingFilterRequest preferences);
    
    /**
     * 매칭 통계 조회
     */
    Object getMatchingStats(UUID userId);
    
    /**
     * 매칭 알고리즘 성능 분석
     */
    Object getMatchingAnalytics(UUID userId);
    
    /**
     * 매칭 이력 기반 추천 개선
     */
    void optimizeMatchingAlgorithm(UUID userId);
    
    /**
     * 특정 시간대/요일 기반 매칭
     */
    Page<RecommendedPartnerResponse> getScheduleBasedMatches(UUID userId, String dayOfWeek, String timeSlot, Pageable pageable);
    
    /**
     * 언어 교환 매칭 (서로의 언어를 배울 수 있는 파트너)
     */
    Page<RecommendedPartnerResponse> getLanguageExchangePartners(UUID userId, Pageable pageable);
    
    /**
     * 매칭 대기열 관리
     */
    void addToMatchingQueue(UUID userId, String sessionType);
    void removeFromMatchingQueue(UUID userId);
    Object getMatchingQueueStatus(UUID userId);
}