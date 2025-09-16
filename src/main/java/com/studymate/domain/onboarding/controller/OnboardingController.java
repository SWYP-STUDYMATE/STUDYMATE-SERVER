package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardingStepRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingProgressResponse;
import com.studymate.domain.onboarding.domain.dto.response.CurrentStepResponse;
import com.studymate.domain.onboarding.service.OnboardingService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Onboarding", description = "사용자 온보딩 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/data")
    public OnboardingDataResponse getOnboardingData(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return onboardingService.getOnboardingData(userId);
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeAllOnboarding(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @RequestBody CompleteAllOnboardingRequest request) {
        UUID userId = principal.getUuid();
        onboardingService.completeAllOnboarding(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getOnboardingProgress(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return ResponseEntity.ok(onboardingService.getOnboardingProgress(userId));
    }

    // === 새로운 UX 개선 API들 ===
    
    @Operation(summary = "온보딩 단계별 저장", description = "각 온보딩 단계를 개별적으로 저장합니다.")
    @PostMapping("/steps/{stepNumber}/save")
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> saveOnboardingStep(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "저장할 단계 번호 (1-7)", required = true)
            @PathVariable Integer stepNumber,
            @Valid @RequestBody OnboardingStepRequest request) {
        
        UUID userId = principal.getUuid();
        OnboardingProgressResponse progress = onboardingService.saveOnboardingStep(userId, stepNumber, request);
        
        return ResponseEntity.ok(ApiResponse.success(progress, 
            String.format("%d/7 단계가 저장되었습니다. 언제든 다시 와서 이어서 진행하세요!", stepNumber)));
    }
    
    @Operation(summary = "현재 온보딩 단계 조회", description = "현재 진행 중인 온보딩 단계와 상세 정보를 조회합니다.")
    @GetMapping("/steps/current")
    public ResponseEntity<ApiResponse<CurrentStepResponse>> getCurrentOnboardingStep(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        CurrentStepResponse currentStep = onboardingService.getCurrentOnboardingStep(userId);
        
        return ResponseEntity.ok(ApiResponse.success(currentStep, "현재 온보딩 단계를 조회했습니다."));
    }
    
    @Operation(summary = "온보딩 단계 건너뛰기", description = "선택적 온보딩 단계를 건너뜁니다.")
    @PostMapping("/steps/{stepNumber}/skip")
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> skipOnboardingStep(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "건너뛸 단계 번호", required = true)
            @PathVariable Integer stepNumber,
            @Parameter(description = "건너뛰는 이유 (선택사항)")
            @RequestParam(required = false) String reason) {
        
        UUID userId = principal.getUuid();
        OnboardingProgressResponse progress = onboardingService.skipOnboardingStep(userId, stepNumber, reason);
        
        return ResponseEntity.ok(ApiResponse.success(progress, 
            String.format("%d단계를 건너뛰었습니다. 나중에 마이페이지에서 설정할 수 있어요.", stepNumber)));
    }
    
    @Operation(summary = "온보딩 단계 되돌아가기", description = "이전 온보딩 단계로 되돌아갑니다.")
    @PostMapping("/steps/{stepNumber}/back")
    public ResponseEntity<ApiResponse<CurrentStepResponse>> goBackToStep(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "되돌아갈 단계 번호", required = true)
            @PathVariable Integer stepNumber) {
        
        UUID userId = principal.getUuid();
        CurrentStepResponse step = onboardingService.goBackToOnboardingStep(userId, stepNumber);
        
        return ResponseEntity.ok(ApiResponse.success(step, "이전 단계로 되돌아갔습니다."));
    }
    
    @Operation(summary = "온보딩 자동 저장", description = "현재 입력 중인 내용을 임시 저장합니다.")
    @PostMapping("/auto-save")
    public ResponseEntity<ApiResponse<Void>> autoSaveOnboarding(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody Map<String, Object> currentData) {
        
        UUID userId = principal.getUuid();
        onboardingService.autoSaveOnboardingData(userId, currentData);
        
        return ResponseEntity.ok(ApiResponse.success("온보딩 데이터가 자동 저장되었습니다."));
    }
    
    @Operation(summary = "체험 매칭 시작", description = "온보딩 미완료 상태에서 매칭을 체험해볼 수 있습니다.")
    @PostMapping("/trial-matching")
    public ResponseEntity<ApiResponse<Object>> startTrialMatching(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        
        // 최소 40% 완료 확인 
        var progress = onboardingService.getOnboardingProgress(userId);
        if (progress.getProgressPercentage() < 40.0) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INSUFFICIENT_ONBOARDING", 
                    "체험 매칭을 위해서는 기본 정보(언어 설정, 관심사)를 완료해주세요."));
        }
        
        Object trialMatching = onboardingService.startTrialMatching(userId);
        
        return ResponseEntity.ok(ApiResponse.success(trialMatching, 
            "체험 매칭이 시작되었습니다! 온보딩을 완료하면 더 정확한 매칭을 받을 수 있어요."));
    }
    
    @Operation(summary = "온보딩 세션 연장", description = "온보딩 세션 시간을 연장합니다.")
    @PostMapping("/extend-session")
    public ResponseEntity<ApiResponse<Void>> extendOnboardingSession(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        UUID userId = principal.getUuid();
        onboardingService.extendOnboardingSession(userId);
        
        return ResponseEntity.ok(ApiResponse.success("온보딩 세션이 30분 연장되었습니다."));
    }
}