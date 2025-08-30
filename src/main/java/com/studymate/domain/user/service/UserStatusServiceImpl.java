package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.domain.repository.UserStatusRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.entity.UserStatus;
import com.studymate.domain.user.domain.dto.response.OnlineStatusResponse;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserStatusServiceImpl implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_STATUS_PREFIX = "user:status:";
    private static final String ONLINE_USERS_KEY = "users:online";
    private static final long INACTIVE_THRESHOLD_MINUTES = 15;

    @Override
    public void setUserOnline(UUID userId, String deviceInfo) {
        UserStatus userStatus = getUserStatusOrCreate(userId);
        userStatus.setOnline(deviceInfo);
        userStatusRepository.save(userStatus);
        
        // Redis 캐시 업데이트
        updateRedisCache(userStatus);
        
        log.debug("User {} set to online with device: {}", userId, deviceInfo);
    }

    @Override
    public void setUserOffline(UUID userId) {
        UserStatus userStatus = getUserStatusOrCreate(userId);
        userStatus.setOffline();
        userStatusRepository.save(userStatus);
        
        // Redis에서 제거
        removeFromRedisCache(userId);
        
        log.debug("User {} set to offline", userId);
    }

    @Override
    public void setUserStudying(UUID userId, UUID sessionId) {
        UserStatus userStatus = getUserStatusOrCreate(userId);
        userStatus.setStudying(sessionId);
        userStatusRepository.save(userStatus);
        
        // Redis 캐시 업데이트
        updateRedisCache(userStatus);
        
        log.debug("User {} set to studying with session: {}", userId, sessionId);
    }

    @Override
    public void setUserAway(UUID userId) {
        UserStatus userStatus = getUserStatusOrCreate(userId);
        userStatus.setStatus(UserStatus.OnlineStatus.AWAY);
        userStatus.setLastSeenAt(LocalDateTime.now());
        userStatusRepository.save(userStatus);
        
        // Redis 캐시 업데이트
        updateRedisCache(userStatus);
        
        log.debug("User {} set to away", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public OnlineStatusResponse getUserStatus(UUID userId) {
        // 먼저 Redis에서 확인
        OnlineStatusResponse cachedStatus = getCachedStatus(userId);
        if (cachedStatus != null) {
            return cachedStatus;
        }
        
        // Redis에 없으면 DB에서 조회
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElse(null);
                
        if (userStatus == null) {
            // UserStatus가 없으면 기본 오프라인 상태로 생성
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
            return createDefaultOfflineStatus(user);
        }
        
        OnlineStatusResponse response = convertToResponse(userStatus);
        
        // 온라인 상태라면 Redis에 캐시
        if (response.isOnline()) {
            updateRedisCache(userStatus);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineStatusResponse> getOnlineUsers() {
        List<UserStatus> onlineUsers = userStatusRepository.findAllOnlineUsers();
        return onlineUsers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineStatusResponse> getStudyingUsers() {
        List<UserStatus> studyingUsers = userStatusRepository.findAllStudyingUsers();
        return studyingUsers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineStatusResponse> getUsersStatus(List<UUID> userIds) {
        List<UserStatus> userStatuses = userStatusRepository.findByUserIds(userIds);
        
        // UserStatus가 없는 사용자들을 위해 기본 상태 생성
        List<UUID> existingUserIds = userStatuses.stream()
                .map(UserStatus::getUserId)
                .collect(Collectors.toList());
                
        List<OnlineStatusResponse> responses = userStatuses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        // 누락된 사용자들에 대해 기본 오프라인 상태 추가
        userIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .forEach(userId -> {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        responses.add(createDefaultOfflineStatus(user));
                    }
                });
                
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public long getOnlineUsersCount() {
        return userStatusRepository.countOnlineUsers();
    }

    @Override
    @Async
    public void cleanupInactiveUsers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(INACTIVE_THRESHOLD_MINUTES);
        int updatedCount = userStatusRepository.markInactiveUsersAsOffline(cutoffTime, LocalDateTime.now());
        
        if (updatedCount > 0) {
            log.info("Marked {} inactive users as offline", updatedCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineStatusResponse> getOnlineUsersByCity(String city) {
        List<UserStatus> onlineUsers = userStatusRepository.findOnlineUsersByCity(city);
        return onlineUsers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Private helper methods
    
    private UserStatus getUserStatusOrCreate(UUID userId) {
        return userStatusRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserStatus(userId));
    }
    
    private UserStatus createNewUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
                
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(userId);
        userStatus.setUser(user);
        userStatus.setStatus(UserStatus.OnlineStatus.OFFLINE);
        userStatus.setLastSeenAt(LocalDateTime.now());
        
        return userStatusRepository.save(userStatus);
    }
    
    private OnlineStatusResponse convertToResponse(UserStatus userStatus) {
        User user = userStatus.getUser();
        
        return OnlineStatusResponse.builder()
                .userId(userStatus.getUserId())
                .englishName(user.getEnglishName())
                .profileImageUrl(user.getProfileImage())
                .status(userStatus.getStatus().name())
                .lastSeenAt(userStatus.getLastSeenAt())
                .deviceInfo(userStatus.getDeviceInfo())
                .isStudying(userStatus.isStudying())
                .currentSessionId(userStatus.getCurrentSessionId())
                .location(user.getLocation() != null ? user.getLocation().getCity() : null)
                .nativeLanguage(user.getNativeLanguage() != null ? user.getNativeLanguage().getName() : null)
                .build();
    }
    
    private OnlineStatusResponse createDefaultOfflineStatus(User user) {
        return OnlineStatusResponse.builder()
                .userId(user.getUserId())
                .englishName(user.getEnglishName())
                .profileImageUrl(user.getProfileImage())
                .status(UserStatus.OnlineStatus.OFFLINE.name())
                .lastSeenAt(null)
                .deviceInfo(null)
                .isStudying(false)
                .currentSessionId(null)
                .location(user.getLocation() != null ? user.getLocation().getCity() : null)
                .nativeLanguage(user.getNativeLanguage() != null ? user.getNativeLanguage().getName() : null)
                .build();
    }
    
    private void updateRedisCache(UserStatus userStatus) {
        try {
            String key = USER_STATUS_PREFIX + userStatus.getUserId();
            OnlineStatusResponse response = convertToResponse(userStatus);
            redisTemplate.opsForValue().set(key, response, 30, TimeUnit.MINUTES);
            
            // 온라인 사용자 목록에도 추가
            if (response.isOnline()) {
                redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userStatus.getUserId().toString());
                redisTemplate.expire(ONLINE_USERS_KEY, 30, TimeUnit.MINUTES);
            } else {
                redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userStatus.getUserId().toString());
            }
            
        } catch (Exception e) {
            log.warn("Failed to update Redis cache for user {}: {}", userStatus.getUserId(), e.getMessage());
        }
    }
    
    private void removeFromRedisCache(UUID userId) {
        try {
            String key = USER_STATUS_PREFIX + userId;
            redisTemplate.delete(key);
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
        } catch (Exception e) {
            log.warn("Failed to remove from Redis cache for user {}: {}", userId, e.getMessage());
        }
    }
    
    private OnlineStatusResponse getCachedStatus(UUID userId) {
        try {
            String key = USER_STATUS_PREFIX + userId;
            return (OnlineStatusResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Failed to get cached status for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}