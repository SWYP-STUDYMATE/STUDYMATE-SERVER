package com.studymate.domain.session.controller;

import com.studymate.domain.session.domain.dto.request.CreateGroupSessionRequest;
import com.studymate.domain.session.domain.dto.request.JoinGroupSessionRequest;
import com.studymate.domain.session.domain.dto.response.GroupSessionResponse;
import com.studymate.domain.session.domain.dto.response.GroupSessionListResponse;
import com.studymate.domain.session.service.GroupSessionService;
import com.studymate.common.dto.ApiResponse;
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

@Tag(name = "Group Session", description = "그룹 세션 API")
@RestController
@RequestMapping("/api/v1/group-sessions")
@RequiredArgsConstructor
public class GroupSessionController {
    
    private final GroupSessionService groupSessionService;
    
    @Operation(summary = "그룹 세션 생성", description = "새로운 그룹 세션을 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupSessionResponse>> createSession(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateGroupSessionRequest request) {
        GroupSessionResponse response = groupSessionService.createSession(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "그룹 세션이 생성되었습니다"));
    }
    
    @Operation(summary = "그룹 세션 참가", description = "세션 ID로 그룹 세션에 참가합니다")
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> joinSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestBody JoinGroupSessionRequest request) {
        GroupSessionResponse response = groupSessionService.joinSession(userId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "세션에 참가했습니다"));
    }
    
    @Operation(summary = "참가 코드로 세션 참가", description = "6자리 참가 코드로 그룹 세션에 참가합니다")
    @PostMapping("/join/{joinCode}")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> joinSessionByCode(
            @AuthenticationPrincipal UUID userId,
            @PathVariable String joinCode,
            @RequestBody JoinGroupSessionRequest request) {
        GroupSessionResponse response = groupSessionService.joinSessionByCode(userId, joinCode, request);
        return ResponseEntity.ok(ApiResponse.success(response, "세션에 참가했습니다"));
    }
    
    @Operation(summary = "그룹 세션 나가기", description = "참가 중인 그룹 세션에서 나갑니다")
    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        groupSessionService.leaveSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "세션에서 나갔습니다"));
    }
    
    @Operation(summary = "세션 시작", description = "호스트가 예약된 세션을 시작합니다")
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> startSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        GroupSessionResponse response = groupSessionService.startSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response, "세션이 시작되었습니다"));
    }
    
    @Operation(summary = "세션 종료", description = "호스트가 진행 중인 세션을 종료합니다")
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> endSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId) {
        GroupSessionResponse response = groupSessionService.endSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response, "세션이 종료되었습니다"));
    }
    
    @Operation(summary = "세션 취소", description = "호스트가 예약된 세션을 취소합니다")
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestParam(required = false) String reason) {
        groupSessionService.cancelSession(userId, sessionId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "세션이 취소되었습니다"));
    }
    
    @Operation(summary = "세션 상세 조회", description = "특정 그룹 세션의 상세 정보를 조회합니다")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> getSessionDetails(
            @PathVariable UUID sessionId) {
        GroupSessionResponse response = groupSessionService.getSessionDetails(sessionId);
        return ResponseEntity.ok(ApiResponse.success(response, "세션 정보를 조회했습니다"));
    }
    
    @Operation(summary = "이용 가능한 세션 목록", description = "참가 가능한 공개 그룹 세션 목록을 조회합니다")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<GroupSessionListResponse>>> getAvailableSessions(
            Pageable pageable,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags) {
        Page<GroupSessionListResponse> response = groupSessionService.getAvailableSessions(
                pageable, language, level, category, tags);
        return ResponseEntity.ok(ApiResponse.success(response, "이용 가능한 세션 목록을 조회했습니다"));
    }
    
    @Operation(summary = "내 세션 목록", description = "사용자가 참가한 세션 목록을 조회합니다")
    @GetMapping("/my-sessions")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String status) {
        List<GroupSessionResponse> response = groupSessionService.getUserSessions(userId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "내 세션 목록을 조회했습니다"));
    }
    
    @Operation(summary = "호스팅한 세션 목록", description = "사용자가 호스팅한 세션 목록을 조회합니다")
    @GetMapping("/hosted")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> getHostedSessions(
            @AuthenticationPrincipal UUID userId) {
        List<GroupSessionResponse> response = groupSessionService.getHostedSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "호스팅한 세션 목록을 조회했습니다"));
    }
    
    @Operation(summary = "참가자 추방", description = "호스트가 문제가 있는 참가자를 세션에서 추방합니다")
    @PostMapping("/{sessionId}/kick/{participantId}")
    public ResponseEntity<ApiResponse<Void>> kickParticipant(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @PathVariable UUID participantId,
            @RequestParam(required = false) String reason) {
        groupSessionService.kickParticipant(userId, sessionId, participantId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "참가자를 추방했습니다"));
    }
    
    @Operation(summary = "세션 평가", description = "참가한 세션에 대해 평가를 남깁니다")
    @PostMapping("/{sessionId}/rate")
    public ResponseEntity<ApiResponse<Void>> rateSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback) {
        groupSessionService.rateSession(userId, sessionId, rating, feedback);
        return ResponseEntity.ok(ApiResponse.success(null, "세션 평가가 저장되었습니다"));
    }
    
    @Operation(summary = "세션 설정 수정", description = "호스트가 세션 설정을 수정합니다")
    @PutMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> updateSessionSettings(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateGroupSessionRequest updateRequest) {
        groupSessionService.updateSessionSettings(userId, sessionId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(null, "세션 설정이 수정되었습니다"));
    }
    
    @Operation(summary = "추천 세션", description = "사용자 맞춤 추천 세션 목록을 조회합니다")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> getRecommendedSessions(
            @AuthenticationPrincipal UUID userId) {
        List<GroupSessionResponse> response = groupSessionService.getRecommendedSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "추천 세션 목록을 조회했습니다"));
    }
    
    @Operation(summary = "세션 초대", description = "호스트가 다른 사용자들을 세션에 초대합니다")
    @PostMapping("/{sessionId}/invite")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> inviteToSession(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestBody List<UUID> invitedUserIds) {
        GroupSessionResponse response = groupSessionService.inviteToSession(userId, sessionId, invitedUserIds);
        return ResponseEntity.ok(ApiResponse.success(response, "초대를 보냈습니다"));
    }
    
    @Operation(summary = "초대 응답", description = "세션 초대에 대해 수락 또는 거절합니다")
    @PostMapping("/{sessionId}/invitation/respond")
    public ResponseEntity<ApiResponse<Void>> respondToInvitation(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sessionId,
            @RequestParam Boolean accept) {
        groupSessionService.respondToInvitation(userId, sessionId, accept);
        String message = accept ? "초대를 수락했습니다" : "초대를 거절했습니다";
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }
    
    @Operation(summary = "세션 검색", description = "키워드로 세션을 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> searchSessions(
            @RequestParam String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String level) {
        List<GroupSessionResponse> response = groupSessionService.searchSessions(keyword, language, level);
        return ResponseEntity.ok(ApiResponse.success(response, "세션 검색 결과를 조회했습니다"));
    }
}