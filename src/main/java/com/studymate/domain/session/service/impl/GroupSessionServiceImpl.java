package com.studymate.domain.session.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.session.domain.dto.request.CreateGroupSessionRequest;
import com.studymate.domain.session.domain.dto.request.JoinGroupSessionRequest;
import com.studymate.domain.session.domain.dto.response.GroupSessionResponse;
import com.studymate.domain.session.domain.dto.response.GroupSessionListResponse;
import com.studymate.domain.session.entity.GroupSession;
import com.studymate.domain.session.entity.GroupSessionParticipant;
import com.studymate.domain.session.repository.GroupSessionRepository;
import com.studymate.domain.session.repository.GroupSessionParticipantRepository;
import com.studymate.domain.session.service.GroupSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupSessionServiceImpl implements GroupSessionService {
    
    private final GroupSessionRepository groupSessionRepository;
    private final GroupSessionParticipantRepository participantRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SESSION_CACHE_PREFIX = "group_session:";
    private static final String USER_ACTIVE_SESSION_PREFIX = "user_session:";
    private static final String SESSION_INVITATION_PREFIX = "session_invite:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);
    
    private final SecureRandom random = new SecureRandom();
    
    @Override
    public GroupSessionResponse createSession(UUID hostUserId, CreateGroupSessionRequest request) {
        validateCreateSessionRequest(request, hostUserId);
        
        GroupSession session = GroupSession.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .hostUserId(hostUserId)
                .topicCategory(request.getTopicCategory())
                .targetLanguage(request.getTargetLanguage())
                .languageLevel(request.getLanguageLevel())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(1)
                .scheduledAt(request.getScheduledAt())
                .sessionDuration(request.getSessionDuration())
                .status(GroupSession.GroupSessionStatus.SCHEDULED)
                .roomId(generateRoomId())
                .sessionTags(joinTags(request.getSessionTags()))
                .isPublic(request.getIsPublic())
                .joinCode(generateJoinCode())
                .ratingAverage(0.0)
                .ratingCount(0)
                .build();
        
        session = groupSessionRepository.save(session);
        
        GroupSessionParticipant hostParticipant = GroupSessionParticipant.builder()
                .sessionId(session.getId())
                .userId(hostUserId)
                .status(GroupSessionParticipant.ParticipantStatus.JOINED)
                .joinedAt(LocalDateTime.now())
                .build();
        
        participantRepository.save(hostParticipant);
        
        cacheSession(session);
        updateUserActiveSession(hostUserId, session.getId());
        
        log.info("Group session created: {} by user: {}", session.getId(), hostUserId);
        
        return buildSessionResponse(session);
    }
    
    @Override
    public GroupSessionResponse joinSession(UUID userId, UUID sessionId, JoinGroupSessionRequest request) {
        GroupSession session = getSessionById(sessionId);
        validateJoinRequest(userId, session);
        
        Optional<GroupSessionParticipant> existingParticipant = 
            participantRepository.findBySessionIdAndUserId(sessionId, userId);
        
        if (existingParticipant.isPresent()) {
            if (existingParticipant.get().getStatus() == GroupSessionParticipant.ParticipantStatus.JOINED) {
                throw new RuntimeException("이미 참가한 세션입니다");
            }
            updateParticipantStatus(existingParticipant.get(), GroupSessionParticipant.ParticipantStatus.JOINED);
        } else {
            GroupSessionParticipant participant = GroupSessionParticipant.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .status(GroupSessionParticipant.ParticipantStatus.JOINED)
                    .joinedAt(LocalDateTime.now())
                    .build();
            participantRepository.save(participant);
        }
        
        session.setCurrentParticipants(participantRepository.countActiveParticipants(sessionId));
        if (session.getCurrentParticipants() >= session.getMaxParticipants() && 
            session.getStatus() == GroupSession.GroupSessionStatus.SCHEDULED) {
            session.setStatus(GroupSession.GroupSessionStatus.WAITING);
        }
        
        groupSessionRepository.save(session);
        cacheSession(session);
        updateUserActiveSession(userId, sessionId);
        
        log.info("User {} joined session {}", userId, sessionId);
        
        return buildSessionResponse(session);
    }
    
    @Override
    public GroupSessionResponse joinSessionByCode(UUID userId, String joinCode, JoinGroupSessionRequest request) {
        GroupSession session = groupSessionRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 참가 코드입니다"));
        
        return joinSession(userId, session.getId(), request);
    }
    
    @Override
    public void leaveSession(UUID userId, UUID sessionId) {
        GroupSession session = getSessionById(sessionId);
        GroupSessionParticipant participant = participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("참가하지 않은 세션입니다"));
        
        if (participant.getStatus() == GroupSessionParticipant.ParticipantStatus.JOINED) {
            participant.setStatus(GroupSessionParticipant.ParticipantStatus.LEFT);
            participant.setLeftAt(LocalDateTime.now());
            
            if (participant.getJoinedAt() != null) {
                long duration = Duration.between(participant.getJoinedAt(), LocalDateTime.now()).toMinutes();
                participant.setParticipationDuration((int) duration);
            }
            
            participantRepository.save(participant);
            
            session.setCurrentParticipants(participantRepository.countActiveParticipants(sessionId));
            
            if (session.getHostUserId().equals(userId)) {
                if (session.getCurrentParticipants() > 0) {
                    transferHostRole(session);
                } else {
                    session.setStatus(GroupSession.GroupSessionStatus.CANCELLED);
                }
            }
            
            groupSessionRepository.save(session);
            cacheSession(session);
            removeUserActiveSession(userId, sessionId);
            
            log.info("User {} left session {}", userId, sessionId);
        }
    }
    
    @Override
    public GroupSessionResponse startSession(UUID hostUserId, UUID sessionId) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        if (session.getStatus() != GroupSession.GroupSessionStatus.SCHEDULED && 
            session.getStatus() != GroupSession.GroupSessionStatus.WAITING) {
            throw new RuntimeException("시작할 수 없는 세션 상태입니다");
        }
        
        session.setStatus(GroupSession.GroupSessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        
        groupSessionRepository.save(session);
        cacheSession(session);
        
        notifySessionStart(session);
        
        log.info("Session {} started by host {}", sessionId, hostUserId);
        
        return buildSessionResponse(session);
    }
    
    @Override
    public GroupSessionResponse endSession(UUID hostUserId, UUID sessionId) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        if (session.getStatus() != GroupSession.GroupSessionStatus.ACTIVE) {
            throw new RuntimeException("활성 상태가 아닌 세션입니다");
        }
        
        session.setStatus(GroupSession.GroupSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        
        updateParticipantsOnSessionEnd(sessionId);
        updateSessionRating(session);
        
        groupSessionRepository.save(session);
        cacheSession(session);
        
        log.info("Session {} ended by host {}", sessionId, hostUserId);
        
        return buildSessionResponse(session);
    }
    
    @Override
    public void cancelSession(UUID hostUserId, UUID sessionId, String reason) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        if (session.getStatus() == GroupSession.GroupSessionStatus.COMPLETED ||
            session.getStatus() == GroupSession.GroupSessionStatus.CANCELLED) {
            throw new RuntimeException("이미 완료되거나 취소된 세션입니다");
        }
        
        session.setStatus(GroupSession.GroupSessionStatus.CANCELLED);
        groupSessionRepository.save(session);
        
        notifySessionCancellation(session, reason);
        clearSessionCache(sessionId);
        
        log.info("Session {} cancelled by host {} - reason: {}", sessionId, hostUserId, reason);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GroupSessionResponse getSessionDetails(UUID sessionId) {
        GroupSession session = getSessionById(sessionId);
        return buildSessionResponse(session);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GroupSessionListResponse> getAvailableSessions(Pageable pageable, String language, String level, String category, List<String> tags) {
        Page<GroupSession> sessions = groupSessionRepository.findAvailablePublicSessions(pageable);
        return sessions.map(this::buildSessionListResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GroupSessionResponse> getUserSessions(UUID userId, String status) {
        List<GroupSessionParticipant> participants = participantRepository.findByUserId(userId);
        
        return participants.stream()
                .map(p -> getSessionById(p.getSessionId()))
                .filter(s -> status == null || s.getStatus().name().equals(status))
                .map(this::buildSessionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GroupSessionResponse> getHostedSessions(UUID userId) {
        List<GroupSession> sessions = groupSessionRepository.findByHostUserId(userId);
        return sessions.stream()
                .map(this::buildSessionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void kickParticipant(UUID hostUserId, UUID sessionId, UUID participantId, String reason) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        GroupSessionParticipant participant = participantRepository.findBySessionIdAndUserId(sessionId, participantId)
                .orElseThrow(() -> new RuntimeException("참가자를 찾을 수 없습니다"));
        
        if (participant.getStatus() == GroupSessionParticipant.ParticipantStatus.JOINED) {
            participant.setStatus(GroupSessionParticipant.ParticipantStatus.KICKED);
            participant.setLeftAt(LocalDateTime.now());
            participantRepository.save(participant);
            
            session.setCurrentParticipants(participantRepository.countActiveParticipants(sessionId));
            groupSessionRepository.save(session);
            
            removeUserActiveSession(participantId, sessionId);
            
            log.info("User {} kicked from session {} by host {} - reason: {}", 
                participantId, sessionId, hostUserId, reason);
        }
    }
    
    @Override
    public void rateSession(UUID userId, UUID sessionId, Integer rating, String feedback) {
        GroupSessionParticipant participant = participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("참가하지 않은 세션입니다"));
        
        participant.setRating(rating);
        participant.setFeedback(feedback);
        participantRepository.save(participant);
        
        updateSessionRating(getSessionById(sessionId));
        
        log.info("User {} rated session {} with {} stars", userId, sessionId, rating);
    }
    
    @Override
    public void updateSessionSettings(UUID hostUserId, UUID sessionId, CreateGroupSessionRequest updateRequest) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        if (session.getStatus() == GroupSession.GroupSessionStatus.ACTIVE ||
            session.getStatus() == GroupSession.GroupSessionStatus.COMPLETED) {
            throw new RuntimeException("활성 또는 완료된 세션은 수정할 수 없습니다");
        }
        
        session.setTitle(updateRequest.getTitle());
        session.setDescription(updateRequest.getDescription());
        session.setTopicCategory(updateRequest.getTopicCategory());
        session.setTargetLanguage(updateRequest.getTargetLanguage());
        session.setLanguageLevel(updateRequest.getLanguageLevel());
        session.setMaxParticipants(updateRequest.getMaxParticipants());
        session.setScheduledAt(updateRequest.getScheduledAt());
        session.setSessionDuration(updateRequest.getSessionDuration());
        session.setSessionTags(joinTags(updateRequest.getSessionTags()));
        session.setIsPublic(updateRequest.getIsPublic());
        
        groupSessionRepository.save(session);
        cacheSession(session);
        
        log.info("Session {} updated by host {}", sessionId, hostUserId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GroupSessionResponse> getRecommendedSessions(UUID userId) {
        List<GroupSession> availableSessions = groupSessionRepository.findByStatus(GroupSession.GroupSessionStatus.SCHEDULED);
        
        return availableSessions.stream()
                .limit(5)
                .map(this::buildSessionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public GroupSessionResponse inviteToSession(UUID hostUserId, UUID sessionId, List<UUID> invitedUserIds) {
        GroupSession session = getSessionById(sessionId);
        validateHostPermission(hostUserId, session);
        
        for (UUID invitedUserId : invitedUserIds) {
            createSessionInvitation(sessionId, invitedUserId, hostUserId);
        }
        
        log.info("Host {} invited {} users to session {}", hostUserId, invitedUserIds.size(), sessionId);
        
        return buildSessionResponse(session);
    }
    
    @Override
    public void respondToInvitation(UUID userId, UUID sessionId, Boolean accept) {
        String invitationKey = SESSION_INVITATION_PREFIX + sessionId + ":" + userId;
        String invitation = redisTemplate.opsForValue().get(invitationKey);
        
        if (invitation == null) {
            throw new RuntimeException("초대를 찾을 수 없습니다");
        }
        
        if (accept) {
            JoinGroupSessionRequest joinRequest = new JoinGroupSessionRequest();
            joinSession(userId, sessionId, joinRequest);
        }
        
        redisTemplate.delete(invitationKey);
        
        log.info("User {} {} invitation to session {}", userId, accept ? "accepted" : "declined", sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GroupSessionResponse> searchSessions(String keyword, String language, String level) {
        List<GroupSession> sessions = groupSessionRepository.findAvailablePublicSessions(Pageable.unpaged()).getContent();
        
        return sessions.stream()
                .filter(s -> matchesSearchCriteria(s, keyword, language, level))
                .map(this::buildSessionResponse)
                .collect(Collectors.toList());
    }
    
    private void validateCreateSessionRequest(CreateGroupSessionRequest request, UUID hostUserId) {
        if (request.getScheduledAt().isBefore(LocalDateTime.now().plusMinutes(10))) {
            throw new RuntimeException("세션은 최소 10분 후부터 예약 가능합니다");
        }
        
        int activeHostSessions = groupSessionRepository.countActiveSessionsByHost(hostUserId);
        if (activeHostSessions >= 3) {
            throw new RuntimeException("동시에 호스팅할 수 있는 세션은 최대 3개입니다");
        }
    }
    
    private void validateJoinRequest(UUID userId, GroupSession session) {
        if (session.getStatus() != GroupSession.GroupSessionStatus.SCHEDULED &&
            session.getStatus() != GroupSession.GroupSessionStatus.WAITING) {
            throw new RuntimeException("참가할 수 없는 세션 상태입니다");
        }
        
        if (session.getCurrentParticipants() >= session.getMaxParticipants()) {
            throw new RuntimeException("세션이 가득 찼습니다");
        }
        
        List<GroupSessionParticipant> userActiveSessions = participantRepository.findActiveSessionsByUser(userId);
        if (userActiveSessions.size() >= 2) {
            throw new RuntimeException("동시에 참가할 수 있는 세션은 최대 2개입니다");
        }
    }
    
    private void validateHostPermission(UUID userId, GroupSession session) {
        if (!session.getHostUserId().equals(userId)) {
            throw new RuntimeException("호스트만 접근 가능합니다");
        }
    }
    
    private GroupSession getSessionById(UUID sessionId) {
        String cacheKey = SESSION_CACHE_PREFIX + sessionId;
        String cachedSession = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedSession != null) {
            try {
                return objectMapper.readValue(cachedSession, GroupSession.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached session: {}", e.getMessage());
            }
        }
        
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        cacheSession(session);
        return session;
    }
    
    private void cacheSession(GroupSession session) {
        try {
            String cacheKey = SESSION_CACHE_PREFIX + session.getId();
            String jsonSession = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(cacheKey, jsonSession, CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache session: {}", e.getMessage());
        }
    }
    
    private void clearSessionCache(UUID sessionId) {
        String cacheKey = SESSION_CACHE_PREFIX + sessionId;
        redisTemplate.delete(cacheKey);
    }
    
    private void updateUserActiveSession(UUID userId, UUID sessionId) {
        String key = USER_ACTIVE_SESSION_PREFIX + userId;
        redisTemplate.opsForSet().add(key, sessionId.toString());
        redisTemplate.expire(key, Duration.ofDays(1));
    }
    
    private void removeUserActiveSession(UUID userId, UUID sessionId) {
        String key = USER_ACTIVE_SESSION_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, sessionId.toString());
    }
    
    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    private String generateRoomId() {
        return "room_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }
    
    private List<String> splitTags(String tags) {
        if (tags == null || tags.isEmpty()) return new ArrayList<>();
        return Arrays.asList(tags.split(","));
    }
    
    private void updateParticipantStatus(GroupSessionParticipant participant, GroupSessionParticipant.ParticipantStatus status) {
        participant.setStatus(status);
        if (status == GroupSessionParticipant.ParticipantStatus.JOINED) {
            participant.setJoinedAt(LocalDateTime.now());
            participant.setLeftAt(null);
        } else if (status == GroupSessionParticipant.ParticipantStatus.LEFT) {
            participant.setLeftAt(LocalDateTime.now());
        }
        participantRepository.save(participant);
    }
    
    private void transferHostRole(GroupSession session) {
        List<GroupSessionParticipant> activeParticipants = 
            participantRepository.findBySessionIdAndStatus(session.getId(), GroupSessionParticipant.ParticipantStatus.JOINED);
        
        if (!activeParticipants.isEmpty()) {
            UUID newHostId = activeParticipants.get(0).getUserId();
            session.setHostUserId(newHostId);
            log.info("Host role transferred to user {} in session {}", newHostId, session.getId());
        }
    }
    
    private void updateParticipantsOnSessionEnd(UUID sessionId) {
        List<GroupSessionParticipant> activeParticipants = 
            participantRepository.findBySessionIdAndStatus(sessionId, GroupSessionParticipant.ParticipantStatus.JOINED);
        
        for (GroupSessionParticipant participant : activeParticipants) {
            if (participant.getJoinedAt() != null) {
                long duration = Duration.between(participant.getJoinedAt(), LocalDateTime.now()).toMinutes();
                participant.setParticipationDuration((int) duration);
                participantRepository.save(participant);
            }
        }
    }
    
    private void updateSessionRating(GroupSession session) {
        Double averageRating = participantRepository.getAverageSessionRating(session.getId());
        int ratingCount = participantRepository.getSessionRatingCount(session.getId());
        
        session.setRatingAverage(averageRating != null ? averageRating : 0.0);
        session.setRatingCount(ratingCount);
        
        groupSessionRepository.save(session);
        cacheSession(session);
    }
    
    private void createSessionInvitation(UUID sessionId, UUID invitedUserId, UUID hostUserId) {
        String invitationKey = SESSION_INVITATION_PREFIX + sessionId + ":" + invitedUserId;
        String invitationData = hostUserId.toString();
        redisTemplate.opsForValue().set(invitationKey, invitationData, Duration.ofDays(3));
    }
    
    private boolean matchesSearchCriteria(GroupSession session, String keyword, String language, String level) {
        boolean matches = true;
        
        if (keyword != null && !keyword.isEmpty()) {
            matches = session.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                     session.getDescription().toLowerCase().contains(keyword.toLowerCase());
        }
        
        if (language != null && !language.isEmpty()) {
            matches = matches && session.getTargetLanguage().equals(language);
        }
        
        if (level != null && !level.isEmpty()) {
            matches = matches && session.getLanguageLevel().equals(level);
        }
        
        return matches;
    }
    
    private void notifySessionStart(GroupSession session) {
        CompletableFuture.runAsync(() -> {
            log.info("Notifying participants of session start: {}", session.getId());
        });
    }
    
    private void notifySessionCancellation(GroupSession session, String reason) {
        CompletableFuture.runAsync(() -> {
            log.info("Notifying participants of session cancellation: {} - reason: {}", session.getId(), reason);
        });
    }
    
    private GroupSessionResponse buildSessionResponse(GroupSession session) {
        List<GroupSessionParticipant> participants = participantRepository.findBySessionId(session.getId());
        
        List<GroupSessionResponse.ParticipantInfo> participantInfos = participants.stream()
                .map(p -> GroupSessionResponse.ParticipantInfo.builder()
                        .userId(p.getUserId())
                        .status(p.getStatus().name())
                        .joinedAt(p.getJoinedAt())
                        .isMuted(p.getIsMuted())
                        .isVideoEnabled(p.getIsVideoEnabled())
                        .build())
                .collect(Collectors.toList());
        
        return GroupSessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .hostUserId(session.getHostUserId())
                .topicCategory(session.getTopicCategory())
                .targetLanguage(session.getTargetLanguage())
                .languageLevel(session.getLanguageLevel())
                .maxParticipants(session.getMaxParticipants())
                .currentParticipants(session.getCurrentParticipants())
                .scheduledAt(session.getScheduledAt())
                .sessionDuration(session.getSessionDuration())
                .status(session.getStatus())
                .roomId(session.getRoomId())
                .sessionTags(splitTags(session.getSessionTags()))
                .isPublic(session.getIsPublic())
                .joinCode(session.getJoinCode())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .ratingAverage(session.getRatingAverage())
                .ratingCount(session.getRatingCount())
                .participants(participantInfos)
                .canJoin(canUserJoinSession(session))
                .build();
    }
    
    private GroupSessionListResponse buildSessionListResponse(GroupSession session) {
        return GroupSessionListResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .topicCategory(session.getTopicCategory())
                .targetLanguage(session.getTargetLanguage())
                .languageLevel(session.getLanguageLevel())
                .maxParticipants(session.getMaxParticipants())
                .currentParticipants(session.getCurrentParticipants())
                .scheduledAt(session.getScheduledAt())
                .sessionDuration(session.getSessionDuration())
                .status(session.getStatus().name())
                .sessionTags(splitTags(session.getSessionTags()))
                .ratingAverage(session.getRatingAverage())
                .ratingCount(session.getRatingCount())
                .canJoin(canUserJoinSession(session))
                .timeUntilStart(formatTimeUntilStart(session.getScheduledAt()))
                .build();
    }
    
    private boolean canUserJoinSession(GroupSession session) {
        return session.getStatus() == GroupSession.GroupSessionStatus.SCHEDULED ||
               session.getStatus() == GroupSession.GroupSessionStatus.WAITING &&
               session.getCurrentParticipants() < session.getMaxParticipants();
    }
    
    private String formatTimeUntilStart(LocalDateTime scheduledAt) {
        Duration duration = Duration.between(LocalDateTime.now(), scheduledAt);
        if (duration.isNegative()) return "시작됨";
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours > 0) {
            return hours + "시간 " + minutes + "분 후";
        } else {
            return minutes + "분 후";
        }
    }
}