package com.studymate.domain.session.controller;

import com.studymate.common.dto.ApiResponse;
import com.studymate.common.dto.PageResponse;
import com.studymate.domain.session.domain.dto.request.BookSessionRequest;
import com.studymate.domain.session.domain.dto.request.CreateSessionRequest;
import com.studymate.domain.session.domain.dto.response.CalendarResponse;
import com.studymate.domain.session.domain.dto.response.SessionBookingResponse;
import com.studymate.domain.session.domain.dto.response.SessionResponse;
import com.studymate.domain.session.service.SessionService;
import com.studymate.domain.session.type.SessionType;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ApiResponse<SessionResponse> createSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateSessionRequest request) {
        UUID userId = principal.getUuid();
        SessionResponse response = sessionService.createSession(userId, request);
        return ApiResponse.success(response, "세션이 성공적으로 생성되었습니다.");
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<SessionResponse> getSession(@PathVariable Long sessionId) {
        SessionResponse response = sessionService.getSession(sessionId);
        return ApiResponse.success(response, "세션 정보를 조회했습니다.");
    }

    @GetMapping("/my-sessions")
    public ApiResponse<PageResponse<SessionResponse>> getUserSessions(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionResponse> response = sessionService.getUserSessions(userId, pageable);
        return ApiResponse.success(PageResponse.of(response), "내 세션 목록을 조회했습니다.");
    }

    @GetMapping("/public")
    public ApiResponse<PageResponse<SessionResponse>> getPublicSessions(Pageable pageable) {
        Page<SessionResponse> response = sessionService.getPublicSessions(pageable);
        return ApiResponse.success(PageResponse.of(response), "공개 세션 목록을 조회했습니다.");
    }

    @GetMapping("/language/{languageCode}")
    public ApiResponse<PageResponse<SessionResponse>> getSessionsByLanguage(
            @PathVariable String languageCode, Pageable pageable) {
        Page<SessionResponse> response = sessionService.getSessionsByLanguage(languageCode, pageable);
        return ApiResponse.success(PageResponse.of(response), "언어별 세션 목록을 조회했습니다.");
    }

    @GetMapping("/type/{sessionType}")
    public ApiResponse<PageResponse<SessionResponse>> getSessionsByType(
            @PathVariable SessionType sessionType, Pageable pageable) {
        Page<SessionResponse> response = sessionService.getSessionsByType(sessionType, pageable);
        return ApiResponse.success(PageResponse.of(response), "유형별 세션 목록을 조회했습니다.");
    }

    @GetMapping("/available")
    public ApiResponse<PageResponse<SessionResponse>> getAvailableSessionsForUser(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionResponse> response = sessionService.getAvailableSessionsForUser(userId, pageable);
        return ApiResponse.success(PageResponse.of(response), "참가 가능한 세션 목록을 조회했습니다.");
    }

    @PostMapping("/book")
    public ApiResponse<SessionBookingResponse> bookSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookSessionRequest request) {
        UUID userId = principal.getUuid();
        SessionBookingResponse response = sessionService.bookSession(userId, request);
        return ApiResponse.success(response, "세션 예약이 완료되었습니다.");
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ApiResponse<Void> cancelBooking(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason) {
        UUID userId = principal.getUuid();
        sessionService.cancelBooking(userId, bookingId, reason);
        return ApiResponse.success(null, "세션 예약이 취소되었습니다.");
    }

    @GetMapping("/my-bookings")
    public ApiResponse<PageResponse<SessionBookingResponse>> getUserBookings(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionBookingResponse> response = sessionService.getUserBookings(userId, pageable);
        return ApiResponse.success(PageResponse.of(response), "내 예약 목록을 조회했습니다.");
    }

    @GetMapping("/calendar")
    public ApiResponse<CalendarResponse> getUserCalendar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        UUID userId = principal.getUuid();
        CalendarResponse response = sessionService.getUserCalendar(userId, startDate, endDate);
        return ApiResponse.success(response, "캘린더 정보를 조회했습니다.");
    }

    @PostMapping("/{sessionId}/start")
    public ApiResponse<Void> startSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId) {
        UUID userId = principal.getUuid();
        sessionService.startSession(userId, sessionId);
        return ApiResponse.success(null, "세션이 시작되었습니다.");
    }

    @PostMapping("/{sessionId}/end")
    public ApiResponse<Void> endSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId) {
        UUID userId = principal.getUuid();
        sessionService.endSession(userId, sessionId);
        return ApiResponse.success(null, "세션이 종료되었습니다.");
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> cancelSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        UUID userId = principal.getUuid();
        sessionService.cancelSession(userId, sessionId, reason);
        return ApiResponse.success(null, "세션이 취소되었습니다.");
    }
}
