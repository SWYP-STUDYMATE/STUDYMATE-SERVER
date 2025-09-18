package com.studymate.domain.session.service;

import com.studymate.domain.session.domain.dto.request.BookSessionRequest;
import com.studymate.domain.session.domain.dto.request.CreateSessionRequest;
import com.studymate.domain.session.domain.dto.response.*;
import com.studymate.domain.session.domain.repository.SessionBookingRepository;
import com.studymate.domain.session.domain.repository.SessionRepository;
import com.studymate.domain.session.entity.Session;
import com.studymate.domain.session.entity.SessionBooking;
import com.studymate.domain.session.type.BookingStatus;
import com.studymate.domain.session.type.SessionStatus;
import com.studymate.domain.session.type.SessionType;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.webrtc.service.WebRTCIntegrationService;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SessionBookingRepository sessionBookingRepository;
    private final WebRTCIntegrationService webRTCIntegrationService;

    @Override
    public SessionResponse createSession(UUID userId, CreateSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        SessionType sessionType = request.getSessionType();
        if (request.getWebRtcRoomType() != null) {
            if ("video".equalsIgnoreCase(request.getWebRtcRoomType())) {
                sessionType = SessionType.VIDEO;
            } else if ("audio".equalsIgnoreCase(request.getWebRtcRoomType())) {
                sessionType = SessionType.AUDIO;
            }
        }

        Session session = Session.builder()
                .hostUser(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .sessionType(sessionType)
                .languageCode(request.getLanguageCode())
                .skillFocus(request.getSkillFocus())
                .levelRequirement(request.getLevelRequirement())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .maxParticipants(request.getMaxParticipants())
                .isRecurring(request.getIsRecurring())
                .recurrencePattern(request.getRecurrencePattern())
                .recurrenceEndDate(request.getRecurrenceEndDate())
                .isPublic(request.getIsPublic())
                .tags(request.getTags())
                .preparationNotes(request.getPreparationNotes())
                .meetingUrl(request.getWebRtcRoomId())
                .build();

        Session savedSession = sessionRepository.save(session);

        if (request.getWebRtcRoomId() != null && !request.getWebRtcRoomId().isBlank()) {
            webRTCIntegrationService.syncRoomOnCreation(request.getWebRtcRoomId(), savedSession);
        }
        
        // 반복 세션인 경우 추가 세션들 생성
        if (request.getIsRecurring() && request.getRecurrencePattern() != null) {
            createRecurringSessions(savedSession, request);
        }

        return convertToSessionResponse(savedSession, user);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));
        
        return convertToSessionResponse(session, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Page<Session> sessions = sessionRepository.findByUserIdOrderByScheduledAtDesc(userId, pageable);
        
        return sessions.map(session -> convertToSessionResponse(session, user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getPublicSessions(Pageable pageable) {
        Page<Session> sessions = sessionRepository.findPublicSessionsByStatus(
                SessionStatus.SCHEDULED, LocalDateTime.now(), pageable);
        
        return sessions.map(session -> convertToSessionResponse(session, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessionsByLanguage(String languageCode, Pageable pageable) {
        Page<Session> sessions = sessionRepository.findByLanguageCodeAndStatus(
                languageCode, SessionStatus.SCHEDULED, LocalDateTime.now(), pageable);
        
        return sessions.map(session -> convertToSessionResponse(session, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessionsByType(SessionType sessionType, Pageable pageable) {
        Page<Session> sessions = sessionRepository.findBySessionTypeAndStatus(
                sessionType, SessionStatus.SCHEDULED, LocalDateTime.now(), pageable);
        
        return sessions.map(session -> convertToSessionResponse(session, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getAvailableSessionsForUser(UUID userId, Pageable pageable) {
        Page<Session> sessions = sessionRepository.findAvailableSessionsForUser(
                userId, SessionStatus.SCHEDULED, LocalDateTime.now(), pageable);
        
        User user = userRepository.findById(userId).orElse(null);
        return sessions.map(session -> convertToSessionResponse(session, user));
    }

    @Override
    public SessionBookingResponse bookSession(UUID userId, BookSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));

        // 예약 가능 여부 확인
        if (!session.canJoin(user)) {
            throw new IllegalStateException("세션에 참여할 수 없습니다.");
        }

        // 중복 예약 확인
        Boolean alreadyBooked = sessionBookingRepository.existsByUserIdAndSessionIdAndStatus(
                userId, request.getSessionId(), BookingStatus.CONFIRMED);
        if (alreadyBooked) {
            throw new IllegalStateException("이미 예약된 세션입니다.");
        }

        // 예약 생성
        SessionBooking booking = SessionBooking.builder()
                .session(session)
                .user(user)
                .bookingMessage(request.getBookingMessage())
                .build();

        SessionBooking savedBooking = sessionBookingRepository.save(booking);

        // 세션 참여자 수 업데이트
        session.addParticipant(user);
        sessionRepository.save(session);

        return convertToSessionBookingResponse(savedBooking);
    }

    @Override
    public void cancelBooking(UUID userId, Long bookingId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        SessionBooking booking = sessionBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND BOOKING"));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("예약을 취소할 권한이 없습니다.");
        }

        if (!booking.canCancel()) {
            throw new IllegalStateException("예약을 취소할 수 없습니다.");
        }

        booking.cancelBooking(reason);
        sessionBookingRepository.save(booking);

        // 세션 참여자 수 감소
        Session session = booking.getSession();
        session.removeParticipant();
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionBookingResponse> getUserBookings(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Page<SessionBooking> bookings = sessionBookingRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return bookings.map(this::convertToSessionBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarResponse getUserCalendar(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Session> sessions = sessionRepository.findSessionsByDateRange(startDate, endDate);
        
        List<CalendarResponse.CalendarEvent> events = sessions.stream()
                .filter(session -> session.getHostUser().getUserId().equals(userId) || 
                                 (session.getGuestUser() != null && session.getGuestUser().getUserId().equals(userId)))
                .map(session -> {
                    boolean isHost = session.getHostUser().getUserId().equals(userId);
                    return new CalendarResponse.CalendarEvent(
                            session.getSessionId(),
                            session.getTitle(),
                            session.getDescription(),
                            session.getScheduledAt(),
                            session.getScheduledAt().plusMinutes(session.getDurationMinutes()),
                            "SESSION",
                            session.getStatus().name(),
                            isHost,
                            isHost ? "#4CAF50" : "#2196F3"
                    );
                })
                .collect(Collectors.toList());

        // 사용 가능한 시간대 계산 (간단한 예시)
        List<CalendarResponse.AvailableTimeSlot> availableSlots = generateAvailableTimeSlots(userId, startDate, endDate);

        return new CalendarResponse(events, availableSlots);
    }

    @Override
    public void startSession(UUID userId, Long sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));

        if (!session.isHost(user)) {
            throw new IllegalArgumentException("세션을 시작할 권한이 없습니다.");
        }

        session.startSession();
        sessionRepository.save(session);
    }

    @Override
    public void endSession(UUID userId, Long sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));

        if (!session.isHost(user)) {
            throw new IllegalArgumentException("세션을 종료할 권한이 없습니다.");
        }

        session.endSession();
        sessionRepository.save(session);
    }

    @Override
    public void cancelSession(UUID userId, Long sessionId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));

        if (!session.isHost(user)) {
            throw new IllegalArgumentException("세션을 취소할 권한이 없습니다.");
        }

        session.cancelSession(reason);
        sessionRepository.save(session);

        // 관련 예약들도 취소 처리
        List<SessionBooking> bookings = sessionBookingRepository.findBySessionOrderByCreatedAtDesc(session);
        bookings.forEach(booking -> {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.cancelBooking("세션이 취소되었습니다: " + reason);
            }
        });
        sessionBookingRepository.saveAll(bookings);
    }

    private void createRecurringSessions(Session originalSession, CreateSessionRequest request) {
        LocalDateTime nextDate = request.getScheduledAt();
        LocalDateTime endDate = request.getRecurrenceEndDate();
        
        while (nextDate.isBefore(endDate)) {
            nextDate = calculateNextRecurrenceDate(nextDate, request.getRecurrencePattern());
            
            if (nextDate.isBefore(endDate)) {
                Session recurringSession = Session.builder()
                        .hostUser(originalSession.getHostUser())
                        .title(originalSession.getTitle())
                        .description(originalSession.getDescription())
                        .sessionType(originalSession.getSessionType())
                        .languageCode(originalSession.getLanguageCode())
                        .skillFocus(originalSession.getSkillFocus())
                        .levelRequirement(originalSession.getLevelRequirement())
                        .scheduledAt(nextDate)
                        .durationMinutes(originalSession.getDurationMinutes())
                        .maxParticipants(originalSession.getMaxParticipants())
                        .isRecurring(true)
                        .recurrencePattern(originalSession.getRecurrencePattern())
                        .recurrenceEndDate(originalSession.getRecurrenceEndDate())
                        .isPublic(originalSession.getIsPublic())
                        .tags(originalSession.getTags())
                        .preparationNotes(originalSession.getPreparationNotes())
                        .meetingUrl(originalSession.getMeetingUrl())
                        .build();
                
                sessionRepository.save(recurringSession);
            }
        }
    }

    private LocalDateTime calculateNextRecurrenceDate(LocalDateTime currentDate, String pattern) {
        switch (pattern) {
            case "DAILY":
                return currentDate.plusDays(1);
            case "WEEKLY":
                return currentDate.plusWeeks(1);
            case "MONTHLY":
                return currentDate.plusMonths(1);
            default:
                return currentDate.plusWeeks(1);
        }
    }

    private List<CalendarResponse.AvailableTimeSlot> generateAvailableTimeSlots(UUID userId, 
                                                                              LocalDateTime startDate, 
                                                                              LocalDateTime endDate) {
        // 실제 구현에서는 사용자의 예약 상황을 고려하여 사용 가능한 시간대를 계산
        // 현재는 간단한 예시만 제공
        List<CalendarResponse.AvailableTimeSlot> slots = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            slots.add(new CalendarResponse.AvailableTimeSlot(
                    current, 
                    current.plusHours(1), 
                    true
            ));
            current = current.plusHours(1);
        }
        
        return slots;
    }

    private SessionResponse convertToSessionResponse(Session session, User currentUser) {
        SessionResponse response = new SessionResponse();
        response.setSessionId(session.getSessionId());
        response.setHostUserId(session.getHostUser().getUserId());
        response.setHostUserName(session.getHostUser().getEnglishName());
        response.setHostUserProfileImage(session.getHostUser().getProfileImage());
        
        if (session.getGuestUser() != null) {
            response.setGuestUserId(session.getGuestUser().getUserId());
            response.setGuestUserName(session.getGuestUser().getEnglishName());
            response.setGuestUserProfileImage(session.getGuestUser().getProfileImage());
        }
        
        response.setTitle(session.getTitle());
        response.setDescription(session.getDescription());
        response.setSessionType(session.getSessionType());
        response.setLanguageCode(session.getLanguageCode());
        response.setSkillFocus(session.getSkillFocus());
        response.setLevelRequirement(session.getLevelRequirement());
        response.setScheduledAt(session.getScheduledAt());
        response.setDurationMinutes(session.getDurationMinutes());
        response.setMaxParticipants(session.getMaxParticipants());
        response.setCurrentParticipants(session.getCurrentParticipants());
        response.setStatus(session.getStatus());
        response.setMeetingUrl(session.getMeetingUrl());
        response.setIsRecurring(session.getIsRecurring());
        response.setRecurrencePattern(session.getRecurrencePattern());
        response.setRecurrenceEndDate(session.getRecurrenceEndDate());
        response.setIsPublic(session.getIsPublic());
        response.setTags(session.getTags());
        response.setPreparationNotes(session.getPreparationNotes());
        response.setStartedAt(session.getStartedAt());
        response.setEndedAt(session.getEndedAt());
        
        if (currentUser != null) {
            response.setCanJoin(session.canJoin(currentUser));
            response.setIsHost(session.isHost(currentUser));
            response.setIsParticipant(session.isParticipant(currentUser));
        }
        
        return response;
    }

    private SessionBookingResponse convertToSessionBookingResponse(SessionBooking booking) {
        SessionBookingResponse response = new SessionBookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setSessionId(booking.getSession().getSessionId());
        response.setSessionTitle(booking.getSession().getTitle());
        response.setSessionScheduledAt(booking.getSession().getScheduledAt());
        response.setSessionDurationMinutes(booking.getSession().getDurationMinutes());
        response.setSessionLanguageCode(booking.getSession().getLanguageCode());
        response.setHostUserId(booking.getSession().getHostUser().getUserId());
        response.setHostUserName(booking.getSession().getHostUser().getEnglishName());
        response.setHostUserProfileImage(booking.getSession().getHostUser().getProfileImage());
        response.setStatus(booking.getStatus());
        response.setBookingMessage(booking.getBookingMessage());
        response.setBookedAt(booking.getCreatedAt());
        response.setCancelledAt(booking.getCancelledAt());
        response.setCancellationReason(booking.getCancellationReason());
        response.setAttended(booking.getAttended());
        response.setFeedbackRating(booking.getFeedbackRating());
        response.setFeedbackComment(booking.getFeedbackComment());
        response.setReminderSent(booking.getReminderSent());
        response.setCanCancel(booking.canCancel());
        
        return response;
    }
}
