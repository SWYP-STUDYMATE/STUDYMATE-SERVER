package com.studymate.domain.session.controller;

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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SessionResponse> createSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateSessionRequest request) {
        UUID userId = principal.getUuid();
        SessionResponse response = sessionService.createSession(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long sessionId) {
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<Page<SessionResponse>> getUserSessions(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionResponse> response = sessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<SessionResponse>> getPublicSessions(Pageable pageable) {
        Page<SessionResponse> response = sessionService.getPublicSessions(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/language/{languageCode}")
    public ResponseEntity<Page<SessionResponse>> getSessionsByLanguage(
            @PathVariable String languageCode, Pageable pageable) {
        Page<SessionResponse> response = sessionService.getSessionsByLanguage(languageCode, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{sessionType}")
    public ResponseEntity<Page<SessionResponse>> getSessionsByType(
            @PathVariable SessionType sessionType, Pageable pageable) {
        Page<SessionResponse> response = sessionService.getSessionsByType(sessionType, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<SessionResponse>> getAvailableSessionsForUser(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionResponse> response = sessionService.getAvailableSessionsForUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/book")
    public ResponseEntity<SessionBookingResponse> bookSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookSessionRequest request) {
        UUID userId = principal.getUuid();
        SessionBookingResponse response = sessionService.bookSession(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason) {
        UUID userId = principal.getUuid();
        sessionService.cancelBooking(userId, bookingId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<Page<SessionBookingResponse>> getUserBookings(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {
        UUID userId = principal.getUuid();
        Page<SessionBookingResponse> response = sessionService.getUserBookings(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getUserCalendar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        UUID userId = principal.getUuid();
        CalendarResponse response = sessionService.getUserCalendar(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<Void> startSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId) {
        UUID userId = principal.getUuid();
        sessionService.startSession(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Void> endSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId) {
        UUID userId = principal.getUuid();
        sessionService.endSession(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelSession(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        UUID userId = principal.getUuid();
        sessionService.cancelSession(userId, sessionId, reason);
        return ResponseEntity.ok().build();
    }
}