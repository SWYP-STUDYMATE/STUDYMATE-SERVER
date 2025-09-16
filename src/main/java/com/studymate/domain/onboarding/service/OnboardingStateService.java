package com.studymate.domain.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingStateService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis 키 패턴
    private static final String ONBOARDING_PREFIX = "onboarding:";
    private static final String STEP_PREFIX = "step:";
    private static final String PROGRESS_PREFIX = "progress:";
    private static final String SESSION_PREFIX = "session:";
    private static final String CURRENT_STEP_KEY = "current_step";
    private static final String COMPLETED_KEY = "completed";
    
    // 만료 시간 설정
    private static final Duration DEFAULT_TTL = Duration.ofDays(7); // 7일간 유지
    private static final Duration SESSION_TTL = Duration.ofHours(2); // 세션은 2시간
    private static final int TOTAL_ONBOARDING_STEPS = 8; // 총 온보딩 단계 수
    
    /**
     * 온보딩 단계별 데이터 저장
     */
    public void saveStepData(UUID userId, Integer stepNumber, Map<String, Object> stepData) {
        String key = buildStepKey(userId, stepNumber);
        
        try {
            // 저장할 데이터에 메타정보 추가
            Map<String, Object> dataWithMeta = new HashMap<>(stepData);
            dataWithMeta.put("stepNumber", stepNumber);
            dataWithMeta.put("savedAt", LocalDateTime.now().toString());
            dataWithMeta.put("isCompleted", true);
            
            String jsonData = objectMapper.writeValueAsString(dataWithMeta);
            
            redisTemplate.opsForValue().set(key, jsonData, DEFAULT_TTL);
            
            // 진행률 업데이트
            updateProgress(userId, stepNumber);
            
            log.info("Onboarding step {} saved for user {}", stepNumber, userId);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize onboarding step data for user {} step {}: {}", 
                userId, stepNumber, e.getMessage(), e);
            throw new RuntimeException("온보딩 데이터 저장에 실패했습니다.", e);
        }
    }
    
    /**
     * 온보딩 단계별 데이터 조회
     */
    public Map<String, Object> getStepData(UUID userId, Integer stepNumber) {
        String key = buildStepKey(userId, stepNumber);
        
        try {
            String jsonData = redisTemplate.opsForValue().get(key);
            
            if (jsonData == null) {
                return new HashMap<>();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> stepData = objectMapper.readValue(jsonData, Map.class);
            
            return stepData;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize onboarding step data for user {} step {}: {}", 
                userId, stepNumber, e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 전체 온보딩 진행률 업데이트
     */
    public void updateProgress(UUID userId, Integer completedStep) {
        String progressKey = buildProgressKey(userId);
        
        try {
            // 현재 진행률 조회
            Map<String, Object> progress = getProgressData(userId).orElse(new HashMap<>());
            
            // 완료된 단계 마킹
            @SuppressWarnings("unchecked")
            Map<String, Boolean> completedSteps = (Map<String, Boolean>) 
                progress.getOrDefault("completedSteps", new HashMap<String, Boolean>());
            
            completedSteps.put("step" + completedStep, true);
            
            // 진행률 계산
            long completedCount = completedSteps.values().stream().mapToLong(completed -> completed ? 1 : 0).sum();
            double progressPercentage = (completedCount / (double) TOTAL_ONBOARDING_STEPS) * 100;
            
            // 진행률 데이터 업데이트
            progress.put("completedSteps", completedSteps);
            progress.put("currentStep", completedStep + 1 > TOTAL_ONBOARDING_STEPS ? TOTAL_ONBOARDING_STEPS : completedStep + 1);
            progress.put("progressPercentage", progressPercentage);
            progress.put("lastUpdatedAt", LocalDateTime.now().toString());
            progress.put("estimatedMinutesRemaining", calculateRemainingTime(completedCount));
            
            String jsonProgress = objectMapper.writeValueAsString(progress);
            redisTemplate.opsForValue().set(progressKey, jsonProgress, DEFAULT_TTL);
            
            log.info("Progress updated for user {}: {}/{} steps completed ({}%)", 
                userId, completedCount, TOTAL_ONBOARDING_STEPS, Math.round(progressPercentage));
                
        } catch (JsonProcessingException e) {
            log.error("Failed to update progress for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 진행률 데이터 조회
     */
    public Optional<Map<String, Object>> getProgressData(UUID userId) {
        String progressKey = buildProgressKey(userId);
        
        try {
            String jsonData = redisTemplate.opsForValue().get(progressKey);
            
            if (jsonData == null) {
                // 새 사용자의 경우 초기 진행률 생성
                return Optional.of(initializeProgress(userId));
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> progressData = objectMapper.readValue(jsonData, Map.class);
            
            return Optional.of(progressData);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize progress data for user {}: {}", userId, e.getMessage(), e);
            return Optional.of(initializeProgress(userId));
        }
    }
    
    /**
     * 온보딩 세션 정보 저장 (자동 저장용)
     */
    public void saveSessionData(UUID userId, Map<String, Object> sessionData) {
        String sessionKey = buildSessionKey(userId);
        
        try {
            sessionData.put("lastAutoSavedAt", LocalDateTime.now().toString());
            String jsonData = objectMapper.writeValueAsString(sessionData);
            
            redisTemplate.opsForValue().set(sessionKey, jsonData, SESSION_TTL);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to save session data for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 온보딩 세션 정보 조회
     */
    public Optional<Map<String, Object>> getSessionData(UUID userId) {
        String sessionKey = buildSessionKey(userId);
        
        try {
            String jsonData = redisTemplate.opsForValue().get(sessionKey);
            
            if (jsonData == null) {
                return Optional.empty();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = objectMapper.readValue(jsonData, Map.class);
            
            return Optional.of(sessionData);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize session data for user {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 특정 단계를 건너뛰기로 표시
     */
    public void markStepAsSkipped(UUID userId, Integer stepNumber, String reason) {
        String key = buildStepKey(userId, stepNumber);
        
        try {
            Map<String, Object> skippedData = new HashMap<>();
            skippedData.put("stepNumber", stepNumber);
            skippedData.put("status", "SKIPPED");
            skippedData.put("reason", reason);
            skippedData.put("skippedAt", LocalDateTime.now().toString());
            
            String jsonData = objectMapper.writeValueAsString(skippedData);
            redisTemplate.opsForValue().set(key, jsonData, DEFAULT_TTL);
            
            // 진행률에 건너뛴 단계도 완료로 처리
            updateProgress(userId, stepNumber);
            
            log.info("Onboarding step {} skipped for user {} with reason: {}", 
                stepNumber, userId, reason);
                
        } catch (JsonProcessingException e) {
            log.error("Failed to mark step as skipped for user {} step {}: {}", 
                userId, stepNumber, e.getMessage(), e);
        }
    }
    
    /**
     * 온보딩 완료 후 Redis 데이터 정리
     */
    public void cleanupOnboardingData(UUID userId) {
        String pattern = ONBOARDING_PREFIX + userId + ":*";
        
        redisTemplate.delete(redisTemplate.keys(pattern));
        
        log.info("Onboarding data cleaned up for user {}", userId);
    }
    
    /**
     * 온보딩 세션 연장
     */
    public void extendSession(UUID userId) {
        String sessionKey = buildSessionKey(userId);
        
        // 기존 세션 데이터 조회
        Optional<Map<String, Object>> sessionData = getSessionData(userId);
        
        if (sessionData.isPresent()) {
            Map<String, Object> data = sessionData.get();
            data.put("extendedAt", LocalDateTime.now().toString());
            data.put("totalExtensions", ((Integer) data.getOrDefault("totalExtensions", 0)) + 1);
            
            saveSessionData(userId, data);
            
            log.info("Onboarding session extended for user {} (total extensions: {})", 
                userId, data.get("totalExtensions"));
        }
    }
    
    /**
     * 현재 온보딩 단계 설정
     */
    public void setCurrentStep(UUID userId, Integer stepNumber) {
        String key = ONBOARDING_PREFIX + userId + ":" + CURRENT_STEP_KEY;
        redisTemplate.opsForValue().set(key, stepNumber.toString(), DEFAULT_TTL);
        log.info("Current step set to {} for user {}", stepNumber, userId);
    }
    
    /**
     * 현재 온보딩 단계 조회
     */
    public Integer getCurrentStep(UUID userId) {
        String key = ONBOARDING_PREFIX + userId + ":" + CURRENT_STEP_KEY;
        String currentStep = redisTemplate.opsForValue().get(key);
        return currentStep != null ? Integer.parseInt(currentStep) : 1;
    }
    
    /**
     * 온보딩 단계 건너뛰기
     */
    public void skipStep(UUID userId, Integer stepNumber) {
        markStepAsSkipped(userId, stepNumber, "사용자가 건너뛰기 선택");
    }
    
    /**
     * 온보딩 상태 조회
     */
    public com.studymate.domain.onboarding.dto.response.OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        Optional<Map<String, Object>> progressData = getProgressData(userId);
        
        if (progressData.isPresent()) {
            Map<String, Object> data = progressData.get();
            Integer currentStep = (Integer) data.get("currentStep");
            Double progressPercentage = (Double) data.get("progressPercentage");
            
            return com.studymate.domain.onboarding.dto.response.OnboardingStatusResponse.builder()
                    .currentStep(currentStep)
                    .totalSteps(TOTAL_ONBOARDING_STEPS)
                    .progressPercentage(progressPercentage)
                    .completed(currentStep >= TOTAL_ONBOARDING_STEPS)
                    .build();
        }
        
        // 초기 상태
        return com.studymate.domain.onboarding.dto.response.OnboardingStatusResponse.builder()
                .currentStep(1)
                .totalSteps(TOTAL_ONBOARDING_STEPS)
                .progressPercentage(0.0)
                .completed(false)
                .build();
    }
    
    /**
     * 온보딩 완료 처리
     */
    public void completeOnboarding(UUID userId) {
        String key = ONBOARDING_PREFIX + userId + ":" + COMPLETED_KEY;
        redisTemplate.opsForValue().set(key, "true", DEFAULT_TTL);
        log.info("Onboarding completed for user {}", userId);
    }
    
    /**
     * 온보딩 완료 여부 확인
     */
    public boolean isOnboardingCompleted(UUID userId) {
        String key = ONBOARDING_PREFIX + userId + ":" + COMPLETED_KEY;
        String completed = redisTemplate.opsForValue().get(key);
        return "true".equals(completed);
    }
    
    /**
     * 온보딩 데이터 삭제 (완료 후 정리)
     */
    public void clearOnboardingData(UUID userId) {
        String pattern = ONBOARDING_PREFIX + userId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("Onboarding data cleared for user {}", userId);
    }
    
    // === Private Helper Methods ===
    
    private String buildStepKey(UUID userId, Integer stepNumber) {
        return ONBOARDING_PREFIX + userId + ":" + STEP_PREFIX + stepNumber;
    }
    
    private String buildProgressKey(UUID userId) {
        return ONBOARDING_PREFIX + userId + ":" + PROGRESS_PREFIX;
    }
    
    private String buildSessionKey(UUID userId) {
        return ONBOARDING_PREFIX + userId + ":" + SESSION_PREFIX;
    }
    
    private Map<String, Object> initializeProgress(UUID userId) {
        Map<String, Object> initialProgress = new HashMap<>();
        initialProgress.put("currentStep", 1);
        initialProgress.put("progressPercentage", 0.0);
        initialProgress.put("completedSteps", new HashMap<String, Boolean>());
        initialProgress.put("startedAt", LocalDateTime.now().toString());
        initialProgress.put("estimatedMinutesRemaining", 25); // 초기 예상 시간
        
        // Redis에 저장
        String progressKey = buildProgressKey(userId);
        try {
            String jsonData = objectMapper.writeValueAsString(initialProgress);
            redisTemplate.opsForValue().set(progressKey, jsonData, DEFAULT_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to initialize progress for user {}: {}", userId, e.getMessage(), e);
        }
        
        return initialProgress;
    }
    
    private Integer calculateRemainingTime(long completedStepsCount) {
        // 각 단계별 평균 소요시간 (분)
        int[] stepTimes = {5, 3, 4, 3, 2, 5, 3}; // 총 25분

        int remainingTime = 0;
        for (int i = (int) completedStepsCount; i < stepTimes.length; i++) {
            remainingTime += stepTimes[i];
        }

        return remainingTime;
    }

}