package com.studymate.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineMessageSyncService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 패턴
    private static final String SYNC_QUEUE_PREFIX = "chat:sync:";
    private static final String SYNC_STATE_PREFIX = "chat:sync:state:";
    private static final String SYNC_CONFLICT_PREFIX = "chat:sync:conflict:";
    private static final String DEVICE_SYNC_PREFIX = "chat:sync:device:";
    
    // 동기화 설정
    private static final Duration SYNC_QUEUE_TTL = Duration.ofDays(7);
    private static final Duration SYNC_STATE_TTL = Duration.ofDays(30);
    private static final Duration CONFLICT_TTL = Duration.ofHours(24);
    
    /**
     * 사용자가 오프라인일 때 메시지를 동기화 큐에 저장
     */
    public void queueMessageForSync(UUID userId, MessageSyncItem message) {
        try {
            // 메시지에 동기화 메타데이터 추가
            message.setSyncId(UUID.randomUUID().toString());
            message.setQueuedAt(LocalDateTime.now());
            message.setSyncStatus(SyncStatus.QUEUED);
            
            String syncKey = buildSyncQueueKey(userId, message.getSyncId());
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            redisTemplate.opsForValue().set(syncKey, jsonMessage, SYNC_QUEUE_TTL);
            
            // 사용자 동기화 상태 업데이트
            updateUserSyncState(userId, message);
            
            log.debug("Message queued for sync: userId={}, syncId={}", userId, message.getSyncId());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to queue message for sync: userId={}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 사용자 온라인 시 동기화 수행
     */
    @Async
    public CompletableFuture<SyncResult> syncOfflineMessages(UUID userId, String deviceId, Long lastSyncTimestamp) {
        try {
            log.info("Starting offline message sync for user {} on device {}", userId, deviceId);
            
            // 동기화 세션 시작
            SyncSession syncSession = startSyncSession(userId, deviceId, lastSyncTimestamp);
            
            // 대기 중인 메시지 조회
            List<MessageSyncItem> queuedMessages = getQueuedMessages(userId, lastSyncTimestamp);
            
            // 충돌 감지 및 해결
            List<MessageSyncItem> resolvedMessages = resolveConflicts(userId, queuedMessages, deviceId);
            
            // 메시지 병합 및 정렬
            List<MessageSyncItem> finalMessages = mergeAndSortMessages(resolvedMessages);
            
            // 동기화 실행
            SyncResult result = executeSyncTasks(syncSession, finalMessages);
            
            // 동기화 완료 처리
            completeSyncSession(syncSession, result);
            
            log.info("Offline message sync completed: userId={}, synced={}, conflicts={}", 
                userId, result.getSyncedCount(), result.getConflictCount());
                
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("Failed to sync offline messages for user {}: {}", userId, e.getMessage(), e);
            
            SyncResult errorResult = SyncResult.builder()
                    .status(SyncStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .syncedCount(0)
                    .conflictCount(0)
                    .build();
                    
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    /**
     * 다중 디바이스 간 메시지 충돌 해결
     */
    public ConflictResolution resolveMessageConflict(UUID userId, List<MessageConflict> conflicts) {
        try {
            List<MessageSyncItem> resolvedMessages = new ArrayList<>();
            List<MessageConflict> unresolvedConflicts = new ArrayList<>();
            
            for (MessageConflict conflict : conflicts) {
                MessageSyncItem resolved = applyConflictResolutionStrategy(conflict);
                
                if (resolved != null) {
                    resolvedMessages.add(resolved);
                    
                    // 해결된 충돌 기록
                    recordResolvedConflict(userId, conflict, resolved);
                } else {
                    unresolvedConflicts.add(conflict);
                    
                    // 수동 해결이 필요한 충돌 저장
                    storeUnresolvedConflict(userId, conflict);
                }
            }
            
            ConflictResolution resolution = ConflictResolution.builder()
                    .userId(userId)
                    .resolvedMessages(resolvedMessages)
                    .unresolvedConflicts(unresolvedConflicts)
                    .resolutionTimestamp(LocalDateTime.now())
                    .autoResolvedCount(resolvedMessages.size())
                    .manualResolutionNeeded(unresolvedConflicts.size())
                    .build();
                    
            log.info("Conflict resolution completed: userId={}, resolved={}, unresolved={}", 
                userId, resolution.getAutoResolvedCount(), resolution.getManualResolutionNeeded());
                
            return resolution;
            
        } catch (Exception e) {
            log.error("Failed to resolve message conflicts for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("메시지 충돌 해결에 실패했습니다.", e);
        }
    }

    /**
     * 디바이스별 동기화 상태 조회
     */
    public DeviceSyncState getDeviceSyncState(UUID userId, String deviceId) {
        try {
            String stateKey = buildDeviceSyncKey(userId, deviceId);
            String jsonState = redisTemplate.opsForValue().get(stateKey);
            
            if (jsonState != null) {
                return objectMapper.readValue(jsonState, DeviceSyncState.class);
            }
            
            // 신규 디바이스인 경우 초기 상태 생성
            DeviceSyncState initialState = DeviceSyncState.builder()
                    .userId(userId)
                    .deviceId(deviceId)
                    .lastSyncAt(LocalDateTime.now())
                    .lastSyncTimestamp(System.currentTimeMillis())
                    .totalSyncedMessages(0)
                    .pendingSyncCount(0)
                    .syncVersion(1L)
                    .build();
                    
            updateDeviceSyncState(initialState);
            return initialState;
            
        } catch (Exception e) {
            log.error("Failed to get device sync state: userId={}, deviceId={}: {}", 
                userId, deviceId, e.getMessage());
            throw new RuntimeException("디바이스 동기화 상태 조회에 실패했습니다.", e);
        }
    }

    /**
     * 동기화 이력 조회
     */
    public List<SyncSession> getSyncHistory(UUID userId, int limit) {
        String pattern = "chat:sync:session:" + userId + ":*";
        Set<String> sessionKeys = redisTemplate.keys(pattern);
        
        if (sessionKeys == null || sessionKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return sessionKeys.stream()
                .limit(limit)
                .map(key -> {
                    try {
                        String jsonSession = redisTemplate.opsForValue().get(key);
                        return jsonSession != null ? 
                            objectMapper.readValue(jsonSession, SyncSession.class) : null;
                    } catch (Exception e) {
                        log.error("Failed to deserialize sync session {}: {}", key, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 대량 메시지 동기화 (백그라운드)
     */
    @Async
    public CompletableFuture<Void> performBulkSync(UUID userId) {
        try {
            log.info("Starting bulk sync for user {}", userId);
            
            // 모든 디바이스의 동기화 상태 조회
            List<DeviceSyncState> deviceStates = getAllDeviceSyncStates(userId);
            
            // 동기화가 필요한 디바이스 식별
            List<DeviceSyncState> devicesNeedingSync = deviceStates.stream()
                    .filter(this::needsSync)
                    .collect(Collectors.toList());
            
            // 각 디바이스별 동기화 수행
            for (DeviceSyncState deviceState : devicesNeedingSync) {
                try {
                    syncOfflineMessages(userId, deviceState.getDeviceId(), deviceState.getLastSyncTimestamp()).get();
                } catch (Exception e) {
                    log.error("Failed to sync device {}: {}", deviceState.getDeviceId(), e.getMessage());
                }
            }
            
            log.info("Bulk sync completed for user {}: {} devices processed", userId, devicesNeedingSync.size());
            
        } catch (Exception e) {
            log.error("Failed to perform bulk sync for user {}: {}", userId, e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // === Private Helper Methods ===
    
    private SyncSession startSyncSession(UUID userId, String deviceId, Long lastSyncTimestamp) {
        SyncSession session = SyncSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                .deviceId(deviceId)
                .lastSyncTimestamp(lastSyncTimestamp)
                .startedAt(LocalDateTime.now())
                .status(SyncStatus.IN_PROGRESS)
                .build();
                
        try {
            String sessionKey = "chat:sync:session:" + userId + ":" + session.getSessionId();
            String jsonSession = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(sessionKey, jsonSession, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            log.error("Failed to save sync session: {}", e.getMessage());
        }
        
        return session;
    }
    
    private List<MessageSyncItem> getQueuedMessages(UUID userId, Long sinceTimestamp) {
        String pattern = buildSyncQueueKey(userId, "*");
        Set<String> messageKeys = redisTemplate.keys(pattern);
        
        if (messageKeys == null || messageKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return messageKeys.stream()
                .map(key -> {
                    try {
                        String jsonMessage = redisTemplate.opsForValue().get(key);
                        return jsonMessage != null ? 
                            objectMapper.readValue(jsonMessage, MessageSyncItem.class) : null;
                    } catch (Exception e) {
                        log.error("Failed to deserialize queued message {}: {}", key, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(msg -> sinceTimestamp == null || msg.getTimestamp() > sinceTimestamp)
                .collect(Collectors.toList());
    }
    
    private List<MessageSyncItem> resolveConflicts(UUID userId, List<MessageSyncItem> messages, String deviceId) {
        // 타임스탬프 기준으로 중복 메시지 감지
        Map<String, List<MessageSyncItem>> duplicateGroups = messages.stream()
                .collect(Collectors.groupingBy(msg -> msg.getMessageId() + ":" + msg.getRoomId()));
        
        List<MessageSyncItem> resolvedMessages = new ArrayList<>();
        
        for (Map.Entry<String, List<MessageSyncItem>> entry : duplicateGroups.entrySet()) {
            List<MessageSyncItem> duplicates = entry.getValue();
            
            if (duplicates.size() == 1) {
                resolvedMessages.add(duplicates.get(0));
            } else {
                // 충돌 해결 전략 적용
                MessageSyncItem resolved = resolveMessageDuplicates(duplicates);
                resolvedMessages.add(resolved);
                
                log.debug("Resolved message conflict: messageId={}, duplicates={}", 
                    resolved.getMessageId(), duplicates.size());
            }
        }
        
        return resolvedMessages;
    }
    
    private MessageSyncItem resolveMessageDuplicates(List<MessageSyncItem> duplicates) {
        // 가장 최근 타임스탬프를 가진 메시지를 선택
        return duplicates.stream()
                .max(Comparator.comparing(MessageSyncItem::getTimestamp))
                .orElse(duplicates.get(0));
    }
    
    private List<MessageSyncItem> mergeAndSortMessages(List<MessageSyncItem> messages) {
        return messages.stream()
                .sorted(Comparator.comparing(MessageSyncItem::getTimestamp))
                .collect(Collectors.toList());
    }
    
    private SyncResult executeSyncTasks(SyncSession session, List<MessageSyncItem> messages) {
        int syncedCount = 0;
        int conflictCount = 0;
        int errorCount = 0;
        
        for (MessageSyncItem message : messages) {
            try {
                boolean synced = syncMessage(session.getUserId(), message);
                if (synced) {
                    syncedCount++;
                    message.setSyncStatus(SyncStatus.COMPLETED);
                } else {
                    conflictCount++;
                    message.setSyncStatus(SyncStatus.CONFLICT);
                }
            } catch (Exception e) {
                errorCount++;
                message.setSyncStatus(SyncStatus.FAILED);
                log.error("Failed to sync message {}: {}", message.getMessageId(), e.getMessage());
            }
        }
        
        return SyncResult.builder()
                .sessionId(session.getSessionId())
                .status(errorCount == 0 ? SyncStatus.COMPLETED : SyncStatus.PARTIAL)
                .syncedCount(syncedCount)
                .conflictCount(conflictCount)
                .errorCount(errorCount)
                .completedAt(LocalDateTime.now())
                .build();
    }
    
    private boolean syncMessage(UUID userId, MessageSyncItem message) {
        // 실제 메시지 동기화 로직
        // TODO: 실제 메시지 저장/업데이트 구현
        
        try {
            // 메시지를 데이터베이스에 저장하거나 업데이트
            // 예: chatMessageRepository.save(convertToEntity(message));
            
            // 동기화 완료 후 큐에서 제거
            String syncKey = buildSyncQueueKey(userId, message.getSyncId());
            redisTemplate.delete(syncKey);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to sync message {}: {}", message.getMessageId(), e.getMessage());
            return false;
        }
    }
    
    private void completeSyncSession(SyncSession session, SyncResult result) {
        session.setStatus(result.getStatus());
        session.setCompletedAt(LocalDateTime.now());
        session.setSyncedCount(result.getSyncedCount());
        session.setErrorCount(result.getErrorCount());
        
        try {
            String sessionKey = "chat:sync:session:" + session.getUserId() + ":" + session.getSessionId();
            String jsonSession = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(sessionKey, jsonSession, Duration.ofDays(7));
        } catch (JsonProcessingException e) {
            log.error("Failed to update sync session: {}", e.getMessage());
        }
    }
    
    private MessageSyncItem applyConflictResolutionStrategy(MessageConflict conflict) {
        // 충돌 해결 전략
        switch (conflict.getConflictType()) {
            case TIMESTAMP_MISMATCH:
                // 서버 타임스탬프 우선
                return conflict.getServerVersion();
            case CONTENT_DIFFERENT:
                // 최근 수정 버전 우선
                return conflict.getLocalVersion().getTimestamp() > conflict.getServerVersion().getTimestamp() 
                    ? conflict.getLocalVersion() : conflict.getServerVersion();
            case DELETION_CONFLICT:
                // 삭제 우선 (삭제가 항상 우선)
                return conflict.getLocalVersion().isDeleted() || conflict.getServerVersion().isDeleted() 
                    ? null : conflict.getServerVersion();
            default:
                return conflict.getServerVersion();
        }
    }
    
    private void recordResolvedConflict(UUID userId, MessageConflict conflict, MessageSyncItem resolved) {
        try {
            ResolvedConflict record = ResolvedConflict.builder()
                    .userId(userId)
                    .conflictId(conflict.getConflictId())
                    .conflictType(conflict.getConflictType())
                    .resolvedMessage(resolved)
                    .resolutionStrategy("AUTO_RESOLUTION")
                    .resolvedAt(LocalDateTime.now())
                    .build();
                    
            String recordKey = "chat:sync:resolved:" + userId + ":" + conflict.getConflictId();
            String jsonRecord = objectMapper.writeValueAsString(record);
            redisTemplate.opsForValue().set(recordKey, jsonRecord, Duration.ofDays(7));
        } catch (JsonProcessingException e) {
            log.error("Failed to record resolved conflict: {}", e.getMessage());
        }
    }
    
    private void storeUnresolvedConflict(UUID userId, MessageConflict conflict) {
        try {
            String conflictKey = buildConflictKey(userId, conflict.getConflictId());
            String jsonConflict = objectMapper.writeValueAsString(conflict);
            redisTemplate.opsForValue().set(conflictKey, jsonConflict, CONFLICT_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to store unresolved conflict: {}", e.getMessage());
        }
    }
    
    private void updateUserSyncState(UUID userId, MessageSyncItem message) {
        try {
            String stateKey = buildSyncStateKey(userId);
            String jsonState = redisTemplate.opsForValue().get(stateKey);
            
            UserSyncState state;
            if (jsonState != null) {
                state = objectMapper.readValue(jsonState, UserSyncState.class);
            } else {
                state = UserSyncState.builder()
                        .userId(userId)
                        .totalQueuedMessages(0)
                        .lastQueuedAt(LocalDateTime.now())
                        .build();
            }
            
            state.setTotalQueuedMessages(state.getTotalQueuedMessages() + 1);
            state.setLastQueuedAt(LocalDateTime.now());
            
            String updatedJson = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(stateKey, updatedJson, SYNC_STATE_TTL);
            
        } catch (Exception e) {
            log.error("Failed to update user sync state: {}", e.getMessage());
        }
    }
    
    private void updateDeviceSyncState(DeviceSyncState state) {
        try {
            String stateKey = buildDeviceSyncKey(state.getUserId(), state.getDeviceId());
            String jsonState = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(stateKey, jsonState, SYNC_STATE_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to update device sync state: {}", e.getMessage());
        }
    }
    
    private List<DeviceSyncState> getAllDeviceSyncStates(UUID userId) {
        String pattern = buildDeviceSyncKey(userId, "*");
        Set<String> stateKeys = redisTemplate.keys(pattern);
        
        if (stateKeys == null || stateKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return stateKeys.stream()
                .map(key -> {
                    try {
                        String jsonState = redisTemplate.opsForValue().get(key);
                        return jsonState != null ? 
                            objectMapper.readValue(jsonState, DeviceSyncState.class) : null;
                    } catch (Exception e) {
                        log.error("Failed to deserialize device sync state {}: {}", key, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private boolean needsSync(DeviceSyncState deviceState) {
        // 마지막 동기화로부터 1시간 이상 경과하거나 대기 중인 메시지가 있는 경우
        return deviceState.getLastSyncAt().isBefore(LocalDateTime.now().minusHours(1)) ||
               deviceState.getPendingSyncCount() > 0;
    }
    
    private String buildSyncQueueKey(UUID userId, String syncId) {
        return SYNC_QUEUE_PREFIX + userId + ":" + syncId;
    }
    
    private String buildSyncStateKey(UUID userId) {
        return SYNC_STATE_PREFIX + userId;
    }
    
    private String buildConflictKey(UUID userId, String conflictId) {
        return SYNC_CONFLICT_PREFIX + userId + ":" + conflictId;
    }
    
    private String buildDeviceSyncKey(UUID userId, String deviceId) {
        return DEVICE_SYNC_PREFIX + userId + ":" + deviceId;
    }
    
    // === 내부 데이터 클래스들 ===
    
    public enum SyncStatus {
        QUEUED, IN_PROGRESS, COMPLETED, FAILED, CONFLICT, PARTIAL
    }
    
    public enum ConflictType {
        TIMESTAMP_MISMATCH, CONTENT_DIFFERENT, DELETION_CONFLICT, DUPLICATE_MESSAGE
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageSyncItem {
        private String syncId;
        private String messageId;
        private String roomId;
        private UUID senderId;
        private String content;
        private Long timestamp;
        private LocalDateTime queuedAt;
        private SyncStatus syncStatus;
        private String messageType;
        private boolean isDeleted;
        private Map<String, Object> metadata;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SyncSession {
        private String sessionId;
        private UUID userId;
        private String deviceId;
        private Long lastSyncTimestamp;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private SyncStatus status;
        private int syncedCount;
        private int errorCount;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SyncResult {
        private String sessionId;
        private SyncStatus status;
        private int syncedCount;
        private int conflictCount;
        private int errorCount;
        private LocalDateTime completedAt;
        private String errorMessage;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageConflict {
        private String conflictId;
        private ConflictType conflictType;
        private MessageSyncItem localVersion;
        private MessageSyncItem serverVersion;
        private LocalDateTime detectedAt;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConflictResolution {
        private UUID userId;
        private List<MessageSyncItem> resolvedMessages;
        private List<MessageConflict> unresolvedConflicts;
        private LocalDateTime resolutionTimestamp;
        private int autoResolvedCount;
        private int manualResolutionNeeded;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeviceSyncState {
        private UUID userId;
        private String deviceId;
        private LocalDateTime lastSyncAt;
        private Long lastSyncTimestamp;
        private int totalSyncedMessages;
        private int pendingSyncCount;
        private Long syncVersion;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserSyncState {
        private UUID userId;
        private int totalQueuedMessages;
        private LocalDateTime lastQueuedAt;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResolvedConflict {
        private UUID userId;
        private String conflictId;
        private ConflictType conflictType;
        private MessageSyncItem resolvedMessage;
        private String resolutionStrategy;
        private LocalDateTime resolvedAt;
    }
}