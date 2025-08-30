package com.studymate.domain.user.service;

import com.studymate.domain.user.entity.UserStatus;
import com.studymate.domain.user.domain.dto.response.OnlineStatusResponse;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    
    void setUserOnline(UUID userId, String deviceInfo);
    
    void setUserOffline(UUID userId);
    
    void setUserStudying(UUID userId, UUID sessionId);
    
    void setUserAway(UUID userId);
    
    OnlineStatusResponse getUserStatus(UUID userId);
    
    List<OnlineStatusResponse> getOnlineUsers();
    
    List<OnlineStatusResponse> getStudyingUsers();
    
    List<OnlineStatusResponse> getUsersStatus(List<UUID> userIds);
    
    long getOnlineUsersCount();
    
    void cleanupInactiveUsers();
    
    List<OnlineStatusResponse> getOnlineUsersByCity(String city);
}