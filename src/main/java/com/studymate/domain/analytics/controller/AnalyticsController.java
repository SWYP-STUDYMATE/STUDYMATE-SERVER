package com.studymate.domain.analytics.controller;

import com.studymate.domain.analytics.domain.dto.response.SystemAnalyticsResponse;
import com.studymate.domain.analytics.domain.dto.response.UserStatsResponse;
import com.studymate.domain.analytics.service.AnalyticsService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "분석 및 통계 대시보드 API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "내 학습 통계 조회",
            description = "현재 사용자의 학습 통계 및 진도를 조회합니다."
    )
    @GetMapping("/users/my-stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getMyStats(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        UserStatsResponse response = analyticsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "학습 통계 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "기간별 내 학습 통계 조회",
            description = "특정 기간 동안의 학습 통계를 조회합니다."
    )
    @GetMapping("/users/my-stats/range")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getMyStatsByDateRange(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "시작 날짜", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID userId = principal.getUuid();
        UserStatsResponse response = analyticsService.getUserStatsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "기간별 학습 통계 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "특정 사용자 통계 조회 (관리자 전용)",
            description = "특정 사용자의 학습 통계를 조회합니다. 관리자만 접근 가능합니다."
    )
    @GetMapping("/users/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable UUID userId) {
        UserStatsResponse response = analyticsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 통계 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "시스템 전체 분석 조회 (관리자 전용)",
            description = "시스템 전체의 분석 통계를 조회합니다. 관리자만 접근 가능합니다."
    )
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemAnalyticsResponse>> getSystemAnalytics() {
        SystemAnalyticsResponse response = analyticsService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(response, "시스템 분석 데이터 조회가 완료되었습니다."));
    }

    @GetMapping("/system/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemAnalyticsResponse> getSystemAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SystemAnalyticsResponse response = analyticsService.getSystemAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자 활동 기록",
            description = "사용자의 활동을 추적하여 분석 데이터로 저장합니다."
    )
    @PostMapping("/activities/record")
    public ResponseEntity<ApiResponse<Void>> recordUserActivity(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "활동 타입 (LOGIN, LOGOUT, SESSION_JOIN 등)", required = true)
            @RequestParam String activityType,
            @Parameter(description = "활동 카테고리 (AUTH, SESSION, CHAT, PROFILE 등)", required = true)
            @RequestParam String activityCategory,
            @Parameter(description = "활동 설명")
            @RequestParam(required = false) String description,
            @Parameter(description = "추가 메타데이터 (JSON 형식)")
            @RequestParam(required = false) String metadata,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        UUID userId = principal.getUuid();
        analyticsService.recordUserActivity(userId, activityType, activityCategory, 
                                          description, metadata, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("사용자 활동이 성공적으로 기록되었습니다."));
    }

    @PostMapping("/learning-progress/update")
    public ResponseEntity<Void> updateLearningProgress(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String languageCode,
            @RequestParam String progressType,
            @RequestParam Integer value,
            @RequestParam(required = false) String metadata) {
        UUID userId = principal.getUuid();
        analyticsService.updateLearningProgress(userId, languageCode, progressType, value, metadata);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> recordSystemMetric(
            @RequestParam String metricName,
            @RequestParam String metricCategory,
            @RequestParam Double metricValue,
            @RequestParam(required = false) String metricUnit,
            @RequestParam(required = false) String aggregationPeriod) {
        analyticsService.recordSystemMetric(metricName, metricCategory, metricValue, 
                                          metricUnit, aggregationPeriod);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/calculate-daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> calculateDailyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        analyticsService.calculateDailyMetrics(date);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/calculate-weekly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> calculateWeeklyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        analyticsService.calculateWeeklyMetrics(weekStart);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/calculate-monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> calculateMonthlyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthStart) {
        analyticsService.calculateMonthlyMetrics(monthStart);
        return ResponseEntity.ok().build();
    }
}