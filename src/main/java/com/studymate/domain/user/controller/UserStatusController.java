package com.studymate.domain.user.controller;

import com.studymate.domain.user.service.UserStatusService;
import com.studymate.domain.user.domain.dto.response.OnlineStatusResponse;
import com.studymate.domain.user.util.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Status", description = "사용자 온라인 상태 관리 API")
@RestController
@RequestMapping("/api/v1/users/status")
@RequiredArgsConstructor
public class UserStatusController {

    private final UserStatusService userStatusService;

    @Operation(summary = "사용자 온라인 상태 설정", description = "현재 사용자를 온라인 상태로 설정합니다.")
    @PostMapping("/online")
    public ResponseEntity<Void> setOnline(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "web") String device) {
        
        userStatusService.setUserOnline(userDetails.getUserId(), device);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 오프라인 상태 설정", description = "현재 사용자를 오프라인 상태로 설정합니다.")
    @PostMapping("/offline")
    public ResponseEntity<Void> setOffline(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        userStatusService.setUserOffline(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 학습 중 상태 설정", description = "현재 사용자를 학습 중 상태로 설정합니다.")
    @PostMapping("/studying")
    public ResponseEntity<Void> setStudying(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID sessionId) {
        
        userStatusService.setUserStudying(userDetails.getUserId(), sessionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 자리비움 상태 설정", description = "현재 사용자를 자리비움 상태로 설정합니다.")
    @PostMapping("/away")
    public ResponseEntity<Void> setAway(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        userStatusService.setUserAway(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 상태 조회", description = "특정 사용자의 온라인 상태를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<OnlineStatusResponse> getUserStatus(
            @Parameter(description = "사용자 ID") @PathVariable UUID userId) {
        
        OnlineStatusResponse status = userStatusService.getUserStatus(userId);
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "내 상태 조회", description = "현재 사용자의 온라인 상태를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<OnlineStatusResponse> getMyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        OnlineStatusResponse status = userStatusService.getUserStatus(userDetails.getUserId());
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "온라인 사용자 목록 조회", description = "현재 온라인 상태인 모든 사용자 목록을 조회합니다.")
    @GetMapping("/online")
    public ResponseEntity<List<OnlineStatusResponse>> getOnlineUsers() {
        List<OnlineStatusResponse> onlineUsers = userStatusService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    @Operation(summary = "학습 중인 사용자 목록 조회", description = "현재 학습 중인 모든 사용자 목록을 조회합니다.")
    @GetMapping("/studying")
    public ResponseEntity<List<OnlineStatusResponse>> getStudyingUsers() {
        List<OnlineStatusResponse> studyingUsers = userStatusService.getStudyingUsers();
        return ResponseEntity.ok(studyingUsers);
    }

    @Operation(summary = "여러 사용자 상태 조회", description = "지정된 사용자들의 온라인 상태를 한 번에 조회합니다.")
    @PostMapping("/batch")
    public ResponseEntity<List<OnlineStatusResponse>> getUsersStatus(
            @RequestBody List<UUID> userIds) {
        
        List<OnlineStatusResponse> statuses = userStatusService.getUsersStatus(userIds);
        return ResponseEntity.ok(statuses);
    }

    @Operation(summary = "온라인 사용자 수 조회", description = "현재 온라인 상태인 사용자의 총 수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<Long> getOnlineUsersCount() {
        long count = userStatusService.getOnlineUsersCount();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "지역별 온라인 사용자 조회", description = "특정 지역의 온라인 사용자 목록을 조회합니다.")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<OnlineStatusResponse>> getOnlineUsersByCity(
            @Parameter(description = "도시명") @PathVariable String city) {
        
        List<OnlineStatusResponse> onlineUsers = userStatusService.getOnlineUsersByCity(city);
        return ResponseEntity.ok(onlineUsers);
    }
}