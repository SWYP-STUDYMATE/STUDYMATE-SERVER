package com.studymate.domain.ai.controller;

import com.studymate.common.dto.ApiResponse;
import com.studymate.domain.ai.domain.dto.request.SendMessageRequest;
import com.studymate.domain.ai.domain.dto.request.StartAiSessionRequest;
import com.studymate.domain.ai.domain.dto.response.AiMessageResponse;
import com.studymate.domain.ai.domain.dto.response.AiPartnerResponse;
import com.studymate.domain.ai.domain.dto.response.AiSessionResponse;
import com.studymate.domain.ai.entity.AiSession;
import com.studymate.domain.ai.service.AiPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Tag(name = "AI Partner", description = "AI 연습 파트너 API")
@RestController
@RequestMapping("/api/v1/ai-partners")
@RequiredArgsConstructor
public class AiPartnerController {
    
    private final AiPartnerService aiPartnerService;
    
    @Operation(summary = "이용 가능한 AI 파트너 조회", description = "언어와 수준에 맞는 AI 파트너 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiPartnerResponse>>> getAvailablePartners(
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String languageLevel) {
        List<AiPartnerResponse> partners = aiPartnerService.getAvailablePartners(targetLanguage, languageLevel);
        return ResponseEntity.ok(ApiResponse.success(partners, "AI 파트너 목록을 조회했습니다"));
    }
    
    @Operation(summary = "추천 AI 파트너", description = "사용자 맞춤 추천 AI 파트너를 조회합니다")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<AiPartnerResponse>>> getRecommendedPartners(
            @AuthenticationPrincipal UUID userId) {
        List<AiPartnerResponse> partners = aiPartnerService.getRecommendedPartners(userId);
        return ResponseEntity.ok(ApiResponse.success(partners, "추천 AI 파트너를 조회했습니다"));
    }
    
    @Operation(summary = "AI 파트너 상세 정보", description = "특정 AI 파트너의 상세 정보를 조회합니다")
    @GetMapping("/{partnerId}")
    public ResponseEntity<ApiResponse<AiPartnerResponse>> getPartnerDetails(
            @PathVariable UUID partnerId) {
        AiPartnerResponse partner = aiPartnerService.getPartnerDetails(partnerId);
        return ResponseEntity.ok(ApiResponse.success(partner, "AI 파트너 정보를 조회했습니다"));
    }
    
    @Operation(summary = "AI 파트너 검색", description = "키워드로 AI 파트너를 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AiPartnerResponse>>> searchPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String personalityType) {
        List<AiPartnerResponse> partners = aiPartnerService.searchPartners(keyword, specialty, personalityType);
        return ResponseEntity.ok(ApiResponse.success(partners, "AI 파트너 검색 결과를 조회했습니다"));
    }
    
    @Operation(summary = "AI 세션 시작", description = "선택한 AI 파트너와 학습 세션을 시작합니다")
    @PostMapping("/sessions/start")
    public ResponseEntity<ApiResponse<AiSessionResponse>> startSession(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody StartAiSessionRequest request) {
        AiSessionResponse session = aiPartnerService.startSession(userId, request);
        return ResponseEntity.ok(ApiResponse.success(session, "AI 세션이 시작되었습니다"));
    }
    
    @Operation(summary = "메시지 전송", description = "AI 파트너에게 메시지를 전송하고 응답을 받습니다")
    @PostMapping("/sessions/message")
    public CompletableFuture<ResponseEntity<ApiResponse<AiMessageResponse>>> sendMessage(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody SendMessageRequest request) {
        return aiPartnerService.sendMessage(userId, request)
                .thenApply(response -> ResponseEntity.ok(ApiResponse.success(response, "메시지를 전송했습니다")));
    }
    
    @Operation(summary = "AI 세션 종료", description = "진행 중인 AI 세션을 종료합니다")
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ApiResponse<AiSessionResponse>> endSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        AiSessionResponse session = aiPartnerService.endSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(session, "AI 세션이 종료되었습니다"));
    }
    
    @Operation(summary = "AI 세션 일시정지", description = "진행 중인 AI 세션을 일시정지합니다")
    @PostMapping("/sessions/{sessionId}/pause")
    public ResponseEntity<ApiResponse<AiSessionResponse>> pauseSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        AiSessionResponse session = aiPartnerService.pauseSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(session, "AI 세션이 일시정지되었습니다"));
    }
    
    @Operation(summary = "AI 세션 재개", description = "일시정지된 AI 세션을 재개합니다")
    @PostMapping("/sessions/{sessionId}/resume")
    public ResponseEntity<ApiResponse<AiSessionResponse>> resumeSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        AiSessionResponse session = aiPartnerService.resumeSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(session, "AI 세션이 재개되었습니다"));
    }
    
    @Operation(summary = "활성 AI 세션 조회", description = "현재 진행 중인 AI 세션을 조회합니다")
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<AiSessionResponse>> getActiveSession(
            @AuthenticationPrincipal UUID userId) {
        AiSessionResponse session = aiPartnerService.getActiveSession(userId);
        if (session != null) {
            return ResponseEntity.ok(ApiResponse.success(session, "활성 AI 세션을 조회했습니다"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(null, "진행 중인 AI 세션이 없습니다"));
        }
    }
    
    @Operation(summary = "내 AI 세션 목록", description = "사용자의 AI 세션 기록을 조회합니다")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<AiSessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UUID userId,
            Pageable pageable) {
        Page<AiSessionResponse> sessions = aiPartnerService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions, "AI 세션 목록을 조회했습니다"));
    }
    
    @Operation(summary = "세션 메시지 기록", description = "특정 AI 세션의 대화 내용을 조회합니다")
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<AiMessageResponse>>> getSessionMessages(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        List<AiMessageResponse> messages = aiPartnerService.getSessionMessages(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages, "세션 메시지를 조회했습니다"));
    }
    
    @Operation(summary = "AI 세션 평가", description = "완료된 AI 세션에 대해 평가를 남깁니다")
    @PostMapping("/sessions/{sessionId}/rate")
    public ResponseEntity<ApiResponse<Void>> rateSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback) {
        aiPartnerService.rateSession(userId, sessionId, rating, feedback);
        return ResponseEntity.ok(ApiResponse.success(null, "세션 평가가 저장되었습니다"));
    }
    
    @Operation(summary = "학습 진도 조회", description = "특정 AI 세션의 학습 진도를 조회합니다")
    @GetMapping("/sessions/{sessionId}/progress")
    public ResponseEntity<ApiResponse<AiSessionResponse.LearningProgress>> getLearningProgress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        AiSessionResponse.LearningProgress progress = aiPartnerService.getLearningProgress(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(progress, "학습 진도를 조회했습니다"));
    }
    
    @Operation(summary = "세션 타입별 조회", description = "특정 학습 타입의 AI 세션들을 조회합니다")
    @GetMapping("/sessions/type/{sessionType}")
    public ResponseEntity<ApiResponse<List<AiSessionResponse>>> getSessionsByType(
            @AuthenticationPrincipal UUID userId,
            @PathVariable AiSession.SessionType sessionType) {
        List<AiSessionResponse> sessions = aiPartnerService.getSessionsByType(userId, sessionType);
        return ResponseEntity.ok(ApiResponse.success(sessions, "세션 타입별 목록을 조회했습니다"));
    }
    
    @Operation(summary = "학습한 어휘 조회", description = "세션에서 학습한 어휘를 조회합니다")
    @GetMapping("/sessions/{sessionId}/vocabulary")
    public ResponseEntity<ApiResponse<List<String>>> getLearnedVocabulary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        List<String> vocabulary = aiPartnerService.getLearnedVocabulary(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(vocabulary, "학습한 어휘를 조회했습니다"));
    }
    
    @Operation(summary = "문법 포인트 조회", description = "세션에서 다룬 문법 포인트를 조회합니다")
    @GetMapping("/sessions/{sessionId}/grammar")
    public ResponseEntity<ApiResponse<List<String>>> getGrammarPoints(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        List<String> grammarPoints = aiPartnerService.getGrammarPoints(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(grammarPoints, "문법 포인트를 조회했습니다"));
    }
}