package com.studymate.domain.leveltest.controller;

import com.studymate.domain.leveltest.domain.dto.request.StartLevelTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.StartVoiceTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.SubmitAnswerRequest;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestResponse;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestSummaryResponse;
import com.studymate.domain.leveltest.domain.dto.response.VoiceAnalysisResponse;
import com.studymate.domain.leveltest.domain.dto.response.VoiceTestPromptResponse;
import com.studymate.domain.leveltest.service.LevelTestService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/level-test")
@RequiredArgsConstructor
public class LevelTestController {

    private final LevelTestService levelTestService;

    @PostMapping("/start")
    public ResponseEntity<LevelTestResponse> startLevelTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody StartLevelTestRequest request) {
        UUID userId = principal.getUuid();
        LevelTestResponse response = levelTestService.startLevelTest(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<LevelTestResponse> submitAnswer(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody SubmitAnswerRequest request) {
        UUID userId = principal.getUuid();
        LevelTestResponse response = levelTestService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{testId}/complete")
    public ResponseEntity<LevelTestResponse> completeLevelTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId) {
        UUID userId = principal.getUuid();
        LevelTestResponse response = levelTestService.completeLevelTest(userId, testId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{testId}")
    public ResponseEntity<LevelTestResponse> getLevelTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId) {
        UUID userId = principal.getUuid();
        LevelTestResponse response = levelTestService.getLevelTest(userId, testId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tests")
    public ResponseEntity<Page<LevelTestResponse>> getUserLevelTests(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<LevelTestResponse> response = levelTestService.getUserLevelTests(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<LevelTestSummaryResponse> getUserLevelTestSummary(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        LevelTestSummaryResponse response = levelTestService.getUserLevelTestSummary(userId);
        return ResponseEntity.ok(response);
    }

    // === 음성 테스트 관련 엔드포인트 ===

    @PostMapping("/voice/start")
    public ResponseEntity<ApiResponse<VoiceTestPromptResponse>> startVoiceTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody StartVoiceTestRequest request) {
        UUID userId = principal.getUuid();
        LevelTestResponse testResponse = levelTestService.startVoiceLevelTest(userId, request.getLanguageCode());
        
        // 음성 테스트 프롬프트 생성
        String prompt = levelTestService.generateVoiceTestPrompt(request.getCurrentLevel(), request.getLanguageCode());
        
        VoiceTestPromptResponse response = VoiceTestPromptResponse.builder()
                .testId(testResponse.getTestId())
                .testType("VOICE_SPEAKING_TEST")
                .languageCode(request.getLanguageCode())
                .currentLevel(request.getCurrentLevel())
                .instructions(prompt)
                .estimatedDurationMinutes(10)
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(response, "음성 테스트가 시작되었습니다."));
    }

    @PostMapping("/voice/{testId}/upload")
    public ResponseEntity<ApiResponse<Void>> uploadVoiceRecording(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId,
            @RequestParam("audio") MultipartFile audioFile) {
        UUID userId = principal.getUuid();
        levelTestService.uploadVoiceRecording(userId, testId, audioFile);
        return ResponseEntity.ok(ApiResponse.success("음성 파일이 업로드되었습니다."));
    }

    @PostMapping("/voice/{testId}/analyze")
    public ResponseEntity<ApiResponse<VoiceAnalysisResponse>> analyzeVoiceTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId) {
        UUID userId = principal.getUuid();
        levelTestService.processVoiceTest(userId, testId);
        
        // 분석 완료 후 결과 조회
        LevelTestResponse testResult = levelTestService.getLevelTest(userId, testId);
        
        VoiceAnalysisResponse analysisResponse = VoiceAnalysisResponse.builder()
                .testId(testResult.getTestId())
                .transcriptText(testResult.getFeedback()) // 임시로 feedback 필드 사용
                .overallScore(testResult.getEstimatedScore())
                .cefrLevel(testResult.getEstimatedLevel())
                .feedback(testResult.getFeedback())
                .strengths(testResult.getStrengths())
                .weaknesses(testResult.getWeaknesses())
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(analysisResponse, "음성 분석이 완료되었습니다."));
    }

    @GetMapping("/voice/{testId}/result")
    public ResponseEntity<ApiResponse<VoiceAnalysisResponse>> getVoiceTestResult(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId) {
        UUID userId = principal.getUuid();
        LevelTestResponse testResult = levelTestService.getLevelTest(userId, testId);
        
        if (!testResult.getIsCompleted()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("TEST_NOT_COMPLETED", "테스트가 아직 완료되지 않았습니다."));
        }
        
        VoiceAnalysisResponse analysisResponse = VoiceAnalysisResponse.builder()
                .testId(testResult.getTestId())
                .transcriptText("분석된 텍스트") // 실제 구현시 transcript 필드에서 가져옴
                .overallScore(testResult.getEstimatedScore())
                .cefrLevel(testResult.getEstimatedLevel())
                .feedback(testResult.getFeedback())
                .strengths(testResult.getStrengths())
                .weaknesses(testResult.getWeaknesses())
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(analysisResponse, "음성 테스트 결과를 조회했습니다."));
    }
}