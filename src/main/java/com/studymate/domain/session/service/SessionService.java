package com.studymate.domain.session.service;

import com.studymate.domain.session.domain.dto.request.BookSessionRequest;
import com.studymate.domain.session.domain.dto.request.CreateSessionRequest;
import com.studymate.domain.session.domain.dto.response.CalendarResponse;
import com.studymate.domain.session.domain.dto.response.SessionBookingResponse;
import com.studymate.domain.session.domain.dto.response.SessionResponse;
import com.studymate.domain.session.type.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionService {
    SessionResponse createSession(UUID userId, CreateSessionRequest request);
    
    SessionResponse getSession(Long sessionId);
    
    Page<SessionResponse> getUserSessions(UUID userId, Pageable pageable);
    
    Page<SessionResponse> getPublicSessions(Pageable pageable);
    
    Page<SessionResponse> getSessionsByLanguage(String languageCode, Pageable pageable);
    
    Page<SessionResponse> getSessionsByType(SessionType sessionType, Pageable pageable);
    
    Page<SessionResponse> getAvailableSessionsForUser(UUID userId, Pageable pageable);
    
    SessionBookingResponse bookSession(UUID userId, BookSessionRequest request);
    
    void cancelBooking(UUID userId, Long bookingId, String reason);
    
    Page<SessionBookingResponse> getUserBookings(UUID userId, Pageable pageable);
    
    CalendarResponse getUserCalendar(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    void startSession(UUID userId, Long sessionId);
    
    void endSession(UUID userId, Long sessionId);
    
    void cancelSession(UUID userId, Long sessionId, String reason);
}