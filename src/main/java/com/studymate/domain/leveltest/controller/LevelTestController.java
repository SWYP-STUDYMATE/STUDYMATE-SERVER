package com.studymate.domain.leveltest.controller;

import com.studymate.common.dto.PageResponse;
import com.studymate.domain.leveltest.domain.dto.request.StartLevelTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.StartVoiceTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.SubmitAnswerRequest;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestResponse;
import com.studymate.domain.leveltest.domain.dto.response.LevelTestSummaryResponse;
import com.studymate.domain.leveltest.domain.dto.response.VoiceAnalysisResponse;
import com.studymate.domain.leveltest.domain.dto.response.VoiceTestPromptResponse;
import com.studymate.domain.leveltest.service.LevelTestService;
import com.studymate.domain.leveltest.domain.repository.LevelTestRepository;
import com.studymate.domain.leveltest.entity.LevelTest;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/level-test")
@RequiredArgsConstructor
public class LevelTestController {

    private final LevelTestService levelTestService;
    private final LevelTestRepository levelTestRepository;

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
    public ResponseEntity<PageResponse<LevelTestResponse>> getUserLevelTests(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<LevelTestResponse> response = levelTestService.getUserLevelTests(userId, pageable);
        return ResponseEntity.ok(PageResponse.of(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<LevelTestSummaryResponse> getUserLevelTestSummary(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        LevelTestSummaryResponse response = levelTestService.getUserLevelTestSummary(userId);
        return ResponseEntity.ok(response);
    }

    // === 음성 테스트 ===

    @PostMapping("/voice/start")
    public ResponseEntity<ApiResponse<VoiceTestPromptResponse>> startVoiceTest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody StartVoiceTestRequest request) {
        UUID userId = principal.getUuid();
        LevelTestResponse testResponse = levelTestService.startVoiceLevelTest(userId, request.getLanguageCode());

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

        // 1) 분석 실행 (권한 체크 포함)
        levelTestService.processVoiceTest(userId, testId);

        // 2) 엔티티로 다시 조회해서 scoreBreakdown까지 정확히 구성
        LevelTest levelTest = levelTestRepository.findByTestIdAndUserId(testId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Level test not found"));

        VoiceAnalysisResponse.ScoreBreakdown sb = VoiceAnalysisResponse.ScoreBreakdown.builder()
                .pronunciationScore(Optional.ofNullable(levelTest.getPronunciationScore()).orElse(0))
                .fluencyScore(Optional.ofNullable(levelTest.getFluencyScore()).orElse(0))
                .grammarScore(Optional.ofNullable(levelTest.getGrammarScore()).orElse(0))
                .vocabularyScore(Optional.ofNullable(levelTest.getVocabularyScore()).orElse(0))
                .build();

        List<String> recList = Optional.ofNullable(levelTest.getRecommendations())
                .map(s -> Arrays.stream(s.split(";"))
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);

        int overall = Optional.ofNullable(levelTest.getEstimatedScore())
                .orElseGet(() -> levelTest.getTotalScore() != null ? levelTest.getTotalScore() :
                        (levelTest.getAccuracyPercentage() != null ? (int) Math.round(levelTest.getAccuracyPercentage()) : 0));

        VoiceAnalysisResponse analysisResponse = VoiceAnalysisResponse.builder()
                .testId(levelTest.getTestId())
                .transcriptText(Optional.ofNullable(levelTest.getTranscriptText()).orElse(""))
                .overallScore(overall)
                .cefrLevel(Optional.ofNullable(levelTest.getEstimatedLevel()).orElse("B1"))
                .feedback(Optional.ofNullable(levelTest.getFeedback()).orElse(""))
                .strengths(Optional.ofNullable(levelTest.getStrengths()).orElse(""))
                .weaknesses(Optional.ofNullable(levelTest.getWeaknesses()).orElse(""))
                .scoreBreakdown(sb)
                .recommendations(recList)
                .build();

        return ResponseEntity.ok(ApiResponse.success(analysisResponse, "음성 분석이 완료되었습니다."));
    }

    @GetMapping("/voice/{testId}/result")
    public ResponseEntity<ApiResponse<VoiceAnalysisResponse>> getVoiceTestResult(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long testId) {
        UUID userId = principal.getUuid();
        LevelTest levelTest = levelTestRepository.findByTestIdAndUserId(testId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Level test not found"));

        if (Boolean.FALSE.equals(levelTest.getIsCompleted())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("TEST_NOT_COMPLETED", "테스트가 아직 완료되지 않았습니다."));
        }

        VoiceAnalysisResponse.ScoreBreakdown sb = VoiceAnalysisResponse.ScoreBreakdown.builder()
                .pronunciationScore(Optional.ofNullable(levelTest.getPronunciationScore()).orElse(0))
                .fluencyScore(Optional.ofNullable(levelTest.getFluencyScore()).orElse(0))
                .grammarScore(Optional.ofNullable(levelTest.getGrammarScore()).orElse(0))
                .vocabularyScore(Optional.ofNullable(levelTest.getVocabularyScore()).orElse(0))
                .build();

        List<String> recList = Optional.ofNullable(levelTest.getRecommendations())
                .map(s -> Arrays.stream(s.split(";"))
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);

        int overall = Optional.ofNullable(levelTest.getEstimatedScore())
                .orElseGet(() -> levelTest.getTotalScore() != null ? levelTest.getTotalScore() :
                        (levelTest.getAccuracyPercentage() != null ? (int) Math.round(levelTest.getAccuracyPercentage()) : 0));

        VoiceAnalysisResponse analysisResponse = VoiceAnalysisResponse.builder()
                .testId(levelTest.getTestId())
                .transcriptText(Optional.ofNullable(levelTest.getTranscriptText()).orElse(""))
                .overallScore(overall)
                .cefrLevel(Optional.ofNullable(levelTest.getEstimatedLevel()).orElse("B1"))
                .feedback(Optional.ofNullable(levelTest.getFeedback()).orElse(""))
                .strengths(Optional.ofNullable(levelTest.getStrengths()).orElse(""))
                .weaknesses(Optional.ofNullable(levelTest.getWeaknesses()).orElse(""))
                .scoreBreakdown(sb)
                .recommendations(recList)
                .build();

        return ResponseEntity.ok(ApiResponse.success(analysisResponse, "음성 테스트 결과를 조회했습니다."));
    }
}
