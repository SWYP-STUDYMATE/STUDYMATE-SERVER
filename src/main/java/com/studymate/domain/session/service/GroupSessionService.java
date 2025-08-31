package com.studymate.domain.session.service;

import com.studymate.domain.session.domain.dto.request.CreateGroupSessionRequest;
import com.studymate.domain.session.domain.dto.request.JoinGroupSessionRequest;
import com.studymate.domain.session.domain.dto.response.GroupSessionResponse;
import com.studymate.domain.session.domain.dto.response.GroupSessionListResponse;
import com.studymate.domain.session.entity.GroupSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GroupSessionService {
    
    GroupSessionResponse createSession(UUID hostUserId, CreateGroupSessionRequest request);
    
    GroupSessionResponse joinSession(UUID userId, UUID sessionId, JoinGroupSessionRequest request);
    
    GroupSessionResponse joinSessionByCode(UUID userId, String joinCode, JoinGroupSessionRequest request);
    
    void leaveSession(UUID userId, UUID sessionId);
    
    GroupSessionResponse startSession(UUID hostUserId, UUID sessionId);
    
    GroupSessionResponse endSession(UUID hostUserId, UUID sessionId);
    
    void cancelSession(UUID hostUserId, UUID sessionId, String reason);
    
    GroupSessionResponse getSessionDetails(UUID sessionId);
    
    Page<GroupSessionListResponse> getAvailableSessions(Pageable pageable, String language, String level, String category, List<String> tags);
    
    List<GroupSessionResponse> getUserSessions(UUID userId, String status);
    
    List<GroupSessionResponse> getHostedSessions(UUID userId);
    
    void kickParticipant(UUID hostUserId, UUID sessionId, UUID participantId, String reason);
    
    void rateSession(UUID userId, UUID sessionId, Integer rating, String feedback);
    
    void updateSessionSettings(UUID hostUserId, UUID sessionId, CreateGroupSessionRequest updateRequest);
    
    List<GroupSessionResponse> getRecommendedSessions(UUID userId);
    
    GroupSessionResponse inviteToSession(UUID hostUserId, UUID sessionId, List<UUID> invitedUserIds);
    
    void respondToInvitation(UUID userId, UUID sessionId, Boolean accept);
    
    List<GroupSessionResponse> searchSessions(String keyword, String language, String level);
}