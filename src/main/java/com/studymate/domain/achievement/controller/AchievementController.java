package com.studymate.domain.achievement.controller;

import com.studymate.common.dto.response.ApiResponse;
import com.studymate.domain.achievement.domain.dto.request.UpdateProgressRequest;
import com.studymate.domain.achievement.domain.dto.response.AchievementResponse;
import com.studymate.domain.achievement.domain.dto.response.AchievementStatsResponse;
import com.studymate.domain.achievement.domain.dto.response.UserAchievementResponse;
import com.studymate.domain.achievement.entity.Achievement;
import com.studymate.domain.achievement.service.AchievementService;
import com.studymate.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievement", description = "성취 시스템 API")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "모든 활성화된 성취 조회", description = "모든 활성화된 성취 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAllAchievements() {
        List<AchievementResponse> achievements = achievementService.getAllActiveAchievements();
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 성취 조회", description = "특정 카테고리의 성취 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAchievementsByCategory(
            @Parameter(description = "성취 카테고리") @PathVariable Achievement.AchievementCategory category) {
        List<AchievementResponse> achievements = achievementService.getAchievementsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    @GetMapping("/my")
    @Operation(summary = "내 성취 현황 조회", description = "사용자의 모든 성취 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getMyAchievements(
            @AuthenticationPrincipal User user) {
        List<UserAchievementResponse> userAchievements = achievementService.getUserAchievements(user);
        return ResponseEntity.ok(ApiResponse.success(userAchievements));
    }

    @GetMapping("/my/completed")
    @Operation(summary = "내 완료된 성취 조회", description = "사용자가 완료한 성취 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getMyCompletedAchievements(
            @AuthenticationPrincipal User user) {
        List<UserAchievementResponse> completedAchievements = achievementService.getCompletedAchievements(user);
        return ResponseEntity.ok(ApiResponse.success(completedAchievements));
    }

    @GetMapping("/my/in-progress")
    @Operation(summary = "내 진행 중인 성취 조회", description = "사용자가 진행 중인 성취 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getMyInProgressAchievements(
            @AuthenticationPrincipal User user) {
        List<UserAchievementResponse> inProgressAchievements = achievementService.getInProgressAchievements(user);
        return ResponseEntity.ok(ApiResponse.success(inProgressAchievements));
    }

    @GetMapping("/my/stats")
    @Operation(summary = "내 성취 통계 조회", description = "사용자의 성취 통계 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<AchievementStatsResponse>> getMyAchievementStats(
            @AuthenticationPrincipal User user) {
        AchievementStatsResponse stats = achievementService.getAchievementStats(user);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/progress")
    @Operation(summary = "성취 진행도 업데이트", description = "특정 성취의 진행도를 업데이트합니다.")
    public ResponseEntity<ApiResponse<UserAchievementResponse>> updateProgress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProgressRequest request) {
        UserAchievementResponse result = achievementService.updateProgress(
            user, request.getAchievementKey(), request.getProgress());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/progress/increment")
    @Operation(summary = "성취 진행도 증가", description = "특정 성취의 진행도를 증가시킵니다.")
    public ResponseEntity<ApiResponse<UserAchievementResponse>> incrementProgress(
            @AuthenticationPrincipal User user,
            @RequestParam String achievementKey,
            @RequestParam(defaultValue = "1") Integer increment) {
        UserAchievementResponse result = achievementService.incrementProgress(user, achievementKey, increment);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{userAchievementId}/claim-reward")
    @Operation(summary = "보상 수령", description = "완료된 성취의 보상을 수령합니다.")
    public ResponseEntity<ApiResponse<UserAchievementResponse>> claimReward(
            @AuthenticationPrincipal User user,
            @Parameter(description = "사용자 성취 ID") @PathVariable Long userAchievementId) {
        UserAchievementResponse result = achievementService.claimReward(user, userAchievementId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/initialize")
    @Operation(summary = "성취 초기화", description = "사용자의 성취를 초기화합니다. (주로 새 사용자 등록시 사용)")
    public ResponseEntity<ApiResponse<String>> initializeAchievements(
            @AuthenticationPrincipal User user) {
        achievementService.initializeUserAchievements(user);
        return ResponseEntity.ok(ApiResponse.success("성취 초기화가 완료되었습니다."));
    }

    @PostMapping("/check-completion")
    @Operation(summary = "성취 완료 확인", description = "사용자의 성취 완료 여부를 확인하고 자동으로 완료 처리합니다.")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> checkCompletion(
            @AuthenticationPrincipal User user) {
        List<UserAchievementResponse> completedAchievements = achievementService.checkAndCompleteAchievements(user);
        return ResponseEntity.ok(ApiResponse.success(completedAchievements));
    }
}