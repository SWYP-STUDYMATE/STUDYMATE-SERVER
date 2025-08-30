package com.studymate.domain.matching.controller;

import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.matching.domain.dto.request.MatchingRequestDto;
import com.studymate.domain.matching.domain.dto.request.RecordFeedbackRequest;
import com.studymate.domain.matching.domain.dto.response.*;
import com.studymate.domain.matching.service.MatchingService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Matching", description = "사용자 매칭 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/partners")
    public Page<RecommendedPartnerResponse> getRecommendedPartners(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable,
            @RequestParam(required = false) String nativeLanguage,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String languageLevel,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge
    ) {
        UUID userId = principal.getUuid();
        return matchingService.getRecommendedPartners(userId, pageable, nativeLanguage, targetLanguage, languageLevel, minAge, maxAge);
    }

    @PostMapping("/request")
    public ResponseEntity<Void> sendMatchingRequest(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @RequestBody MatchingRequestDto request) {
        UUID userId = principal.getUuid();
        matchingService.sendMatchingRequest(userId, request.getTargetUserId(), request.getMessage());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/sent")
    public Page<SentMatchingRequestResponse> getSentMatchingRequests(@AuthenticationPrincipal CustomUserDetails principal,
                                                                    Pageable pageable) {
        UUID userId = principal.getUuid();
        return matchingService.getSentMatchingRequests(userId, pageable);
    }

    @GetMapping("/requests/received")
    public Page<ReceivedMatchingRequestResponse> getReceivedMatchingRequests(@AuthenticationPrincipal CustomUserDetails principal,
                                                                           Pageable pageable) {
        UUID userId = principal.getUuid();
        return matchingService.getReceivedMatchingRequests(userId, pageable);
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Void> acceptMatchingRequest(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable UUID requestId) {
        UUID userId = principal.getUuid();
        matchingService.acceptMatchingRequest(userId, requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Void> rejectMatchingRequest(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable UUID requestId) {
        UUID userId = principal.getUuid();
        matchingService.rejectMatchingRequest(userId, requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/matches")
    public Page<MatchedPartnerResponse> getMatchedPartners(@AuthenticationPrincipal CustomUserDetails principal,
                                                          Pageable pageable) {
        UUID userId = principal.getUuid();
        return matchingService.getMatchedPartners(userId, pageable);
    }

    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> removeMatch(@AuthenticationPrincipal CustomUserDetails principal,
                                           @PathVariable UUID matchId) {
        UUID userId = principal.getUuid();
        matchingService.removeMatch(userId, matchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/compatibility/{targetUserId}")
    public CompatibilityScoreResponse getCompatibilityScore(@AuthenticationPrincipal CustomUserDetails principal,
                                                           @PathVariable UUID targetUserId) {
        UUID userId = principal.getUuid();
        return matchingService.getCompatibilityScore(userId, targetUserId);
    }

    @Operation(summary = "고급 필터를 사용한 파트너 추천", description = "다양한 필터 조건을 사용하여 최적화된 파트너를 추천합니다.")
    @PostMapping("/partners/advanced")
    public ResponseEntity<Page<RecommendedPartnerResponse>> getRecommendedPartnersAdvanced(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody AdvancedMatchingFilterRequest filters,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> partners = matchingService.getRecommendedPartnersAdvanced(userId, filters, pageable);
        return ResponseEntity.ok(partners);
    }

    @Operation(summary = "온라인 파트너 추천", description = "현재 온라인 상태인 사용자들 중에서 파트너를 추천합니다.")
    @PostMapping("/partners/online")
    public ResponseEntity<Page<RecommendedPartnerResponse>> getOnlinePartners(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) AdvancedMatchingFilterRequest filters,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> partners = matchingService.getOnlinePartners(userId, filters, pageable);
        return ResponseEntity.ok(partners);
    }

    @Operation(summary = "즉석 매칭", description = "현재 온라인이고 매칭을 원하는 사용자들과 즉시 매칭합니다.")
    @GetMapping("/partners/instant")
    public ResponseEntity<Page<RecommendedPartnerResponse>> getInstantMatching(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String nativeLanguage,
            @RequestParam(required = false) String city,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        
        // 즉석 매칭을 위한 기본 필터 생성
        AdvancedMatchingFilterRequest filters = AdvancedMatchingFilterRequest.builder()
                .onlineOnly(true)
                .nativeLanguage(nativeLanguage)
                .city(city)
                .sortBy("lastactive")
                .sortDirection("desc")
                .limit(10) // 즉석 매칭은 10명으로 제한
                .build();
        
        Page<RecommendedPartnerResponse> partners = matchingService.getOnlinePartners(userId, filters, pageable);
        return ResponseEntity.ok(partners);
    }

    // === AI 기반 스마트 매칭 ===
    
    @Operation(summary = "AI 스마트 매칭", description = "사용자 행동 패턴과 선호도를 학습하여 최적화된 파트너를 추천합니다.")
    @GetMapping("/smart-recommendations")
    public ResponseEntity<ApiResponse<Page<RecommendedPartnerResponse>>> getSmartRecommendations(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> recommendations = matchingService.getSmartRecommendations(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(recommendations, "AI 기반 스마트 매칭 추천을 성공적으로 조회했습니다."));
    }

    @Operation(summary = "실시간 매칭", description = "현재 온라인인 사용자들 중에서 즉시 매칭 가능한 파트너를 찾습니다.")
    @GetMapping("/real-time")
    public ResponseEntity<ApiResponse<Page<RecommendedPartnerResponse>>> getRealTimeMatches(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "ANY") String sessionType) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> realTimeMatches = matchingService.getRealTimeMatches(userId, sessionType);
        return ResponseEntity.ok(ApiResponse.success(realTimeMatches, "실시간 매칭 파트너를 성공적으로 조회했습니다."));
    }

    // === 매칭 피드백 시스템 ===
    
    @Operation(summary = "매칭 품질 피드백", description = "매칭된 파트너에 대한 피드백을 제출합니다.")
    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<Void>> recordMatchingFeedback(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Parameter(description = "매칭 피드백 정보") RecordFeedbackRequest request) {
        
        UUID userId = principal.getUuid();
        matchingService.recordMatchingFeedback(userId, request.getPartnerId(), request.getQualityScore(), request.getFeedback());
        return ResponseEntity.ok(ApiResponse.success("매칭 피드백이 성공적으로 등록되었습니다."));
    }

    // === 매칭 선호도 업데이트 ===
    
    @Operation(summary = "매칭 선호도 업데이트", description = "사용자의 매칭 선호도를 업데이트합니다.")
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<Void>> updateMatchingPreferences(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody AdvancedMatchingFilterRequest preferences) {
        
        UUID userId = principal.getUuid();
        matchingService.updateMatchingPreferences(userId, preferences);
        return ResponseEntity.ok(ApiResponse.success("매칭 선호도가 성공적으로 업데이트되었습니다."));
    }

    // === 매칭 통계 및 분석 ===
    
    @Operation(summary = "매칭 통계 조회", description = "사용자의 매칭 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getMatchingStats(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        Object stats = matchingService.getMatchingStats(userId);
        return ResponseEntity.ok(ApiResponse.success(stats, "매칭 통계를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "매칭 알고리즘 성능 분석", description = "매칭 알고리즘의 성능을 분석합니다.")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Object>> getMatchingAnalytics(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        Object analytics = matchingService.getMatchingAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "매칭 알고리즘 분석 결과를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "매칭 알고리즘 최적화", description = "매칭 이력을 기반으로 알고리즘을 최적화합니다.")
    @PostMapping("/optimize")
    public ResponseEntity<ApiResponse<Void>> optimizeMatchingAlgorithm(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        matchingService.optimizeMatchingAlgorithm(userId);
        return ResponseEntity.ok(ApiResponse.success("매칭 알고리즘이 성공적으로 최적화되었습니다."));
    }

    // === 스케줄 기반 매칭 ===
    
    @Operation(summary = "스케줄 기반 매칭", description = "특정 시간대/요일 기반으로 파트너를 매칭합니다.")
    @GetMapping("/schedule-based")
    public ResponseEntity<ApiResponse<Page<RecommendedPartnerResponse>>> getScheduleBasedMatches(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String dayOfWeek,
            @RequestParam String timeSlot,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> scheduleMatches = matchingService.getScheduleBasedMatches(userId, dayOfWeek, timeSlot, pageable);
        return ResponseEntity.ok(ApiResponse.success(scheduleMatches, "스케줄 기반 매칭 파트너를 성공적으로 조회했습니다."));
    }

    // === 언어 교환 매칭 ===
    
    @Operation(summary = "언어 교환 매칭", description = "서로의 언어를 배울 수 있는 파트너를 매칭합니다.")
    @GetMapping("/language-exchange")
    public ResponseEntity<ApiResponse<Page<RecommendedPartnerResponse>>> getLanguageExchangePartners(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        
        UUID userId = principal.getUuid();
        Page<RecommendedPartnerResponse> languageExchangePartners = matchingService.getLanguageExchangePartners(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(languageExchangePartners, "언어 교환 파트너를 성공적으로 조회했습니다."));
    }

    // === 매칭 대기열 관리 ===
    
    @Operation(summary = "매칭 대기열 참가", description = "매칭 대기열에 참가합니다.")
    @PostMapping("/queue/join")
    public ResponseEntity<ApiResponse<Void>> addToMatchingQueue(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "ANY") String sessionType) {
        
        UUID userId = principal.getUuid();
        matchingService.addToMatchingQueue(userId, sessionType);
        return ResponseEntity.ok(ApiResponse.success("매칭 대기열에 성공적으로 참가했습니다."));
    }

    @Operation(summary = "매칭 대기열 탈퇴", description = "매칭 대기열에서 탈퇴합니다.")
    @PostMapping("/queue/leave")
    public ResponseEntity<ApiResponse<Void>> removeFromMatchingQueue(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        matchingService.removeFromMatchingQueue(userId);
        return ResponseEntity.ok(ApiResponse.success("매칭 대기열에서 성공적으로 탈퇴했습니다."));
    }

    @Operation(summary = "매칭 대기열 상태 조회", description = "현재 매칭 대기열 상태를 조회합니다.")
    @GetMapping("/queue/status")
    public ResponseEntity<ApiResponse<Object>> getMatchingQueueStatus(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        Object queueStatus = matchingService.getMatchingQueueStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(queueStatus, "매칭 대기열 상태를 성공적으로 조회했습니다."));
    }
}