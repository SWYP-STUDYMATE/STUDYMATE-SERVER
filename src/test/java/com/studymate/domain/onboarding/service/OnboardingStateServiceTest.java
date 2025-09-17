package com.studymate.domain.onboarding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.common.exception.ErrorCode;
import com.studymate.domain.onboarding.exception.OnboardingBusinessException;
import com.studymate.domain.user.domain.dto.response.OnboardingStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingStateService 단위 테스트")
class OnboardingStateServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OnboardingStateService onboardingStateService;

    private UUID userId;
    private final Duration DEFAULT_TTL = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("단계별 데이터 저장 성공")
    void saveStepData_Success() throws Exception {
        // Given
        Integer stepNumber = 1;
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("interests", "programming,music");
        stepData.put("level", "INTERMEDIATE");

        String expectedJson = "{\"interests\":\"programming,music\",\"level\":\"INTERMEDIATE\"}";
        when(objectMapper.writeValueAsString(stepData)).thenReturn(expectedJson);

        // When
        onboardingStateService.saveStepData(userId, stepNumber, stepData);

        // Then
        String expectedKey = "onboarding:" + userId.toString() + ":step:" + stepNumber;
        verify(valueOperations).set(expectedKey, expectedJson, DEFAULT_TTL);
        verify(objectMapper).writeValueAsString(stepData);
    }

    @Test
    @DisplayName("단계별 데이터 조회 성공")
    void getStepData_Success() throws Exception {
        // Given
        Integer stepNumber = 2;
        String expectedKey = "onboarding:" + userId.toString() + ":step:" + stepNumber;
        String storedJson = "{\"language\":\"KOREAN\",\"level\":\"BEGINNER\"}";
        
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("language", "KOREAN");
        expectedData.put("level", "BEGINNER");

        when(valueOperations.get(expectedKey)).thenReturn(storedJson);
        when(objectMapper.readValue(eq(storedJson), eq(Map.class))).thenReturn(expectedData);

        // When
        Map<String, Object> result = onboardingStateService.getStepData(userId, stepNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("language")).isEqualTo("KOREAN");
        assertThat(result.get("level")).isEqualTo("BEGINNER");
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("존재하지 않는 단계 데이터 조회 시 빈 맵 반환")
    void getStepData_NotFound_ReturnsEmptyMap() throws Exception {
        // Given
        Integer stepNumber = 3;
        String expectedKey = "onboarding:" + userId.toString() + ":step:" + stepNumber;
        
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // When
        Map<String, Object> result = onboardingStateService.getStepData(userId, stepNumber);

        // Then
        assertThat(result).isEmpty();
        verify(valueOperations).get(expectedKey);
        verify(objectMapper, never()).readValue(anyString(), eq(Map.class));
    }

    @Test
    @DisplayName("현재 온보딩 단계 설정")
    void setCurrentStep_Success() {
        // Given
        Integer currentStep = 4;

        // When
        onboardingStateService.setCurrentStep(userId, currentStep);

        // Then
        String expectedKey = "onboarding:" + userId.toString() + ":current_step";
        verify(valueOperations).set(expectedKey, currentStep.toString(), DEFAULT_TTL);
    }

    @Test
    @DisplayName("현재 온보딩 단계 조회")
    void getCurrentStep_Success() {
        // Given
        Integer expectedStep = 3;
        String expectedKey = "onboarding:" + userId.toString() + ":current_step";
        
        when(valueOperations.get(expectedKey)).thenReturn(expectedStep.toString());

        // When
        Integer result = onboardingStateService.getCurrentStep(userId);

        // Then
        assertThat(result).isEqualTo(expectedStep);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("현재 단계 미설정 시 1 반환")
    void getCurrentStep_NotSet_ReturnsOne() {
        // Given
        String expectedKey = "onboarding:" + userId.toString() + ":current_step";
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // When
        Integer result = onboardingStateService.getCurrentStep(userId);

        // Then
        assertThat(result).isEqualTo(1);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("단계 건너뛰기 성공")
    void skipStep_Success() {
        // Given
        Integer stepNumber = 5;

        // When
        onboardingStateService.skipStep(userId, stepNumber);

        // Then
        String expectedKey = "onboarding:" + userId.toString() + ":step:" + stepNumber;
        String expectedValue = "{\"skipped\":true}";
        verify(valueOperations).set(expectedKey, expectedValue, DEFAULT_TTL);
    }

    @Test
    @DisplayName("온보딩 상태 조회 성공")
    void getOnboardStatus_Success() {
        // Given
        String currentStepKey = "onboarding:" + userId.toString() + ":current_step";
        when(valueOperations.get(currentStepKey)).thenReturn("3");

        // When
        OnboardingStatusResponse result = onboardingStateService.getOnboardingStatus(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStep()).isEqualTo(3);
        assertThat(result.getTotalSteps()).isEqualTo(8);
        assertThat(result.getProgressPercentage()).isEqualTo(37.5);
        assertThat(result.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("온보딩 완료 상태 조회")
    void getOnboardStatus_Completed() {
        // Given
        String currentStepKey = "onboarding:" + userId.toString() + ":current_step";
        when(valueOperations.get(currentStepKey)).thenReturn("8");

        // When
        OnboardingStatusResponse result = onboardingStateService.getOnboardingStatus(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStep()).isEqualTo(8);
        assertThat(result.getTotalSteps()).isEqualTo(8);
        assertThat(result.getProgressPercentage()).isEqualTo(100.0);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("온보딩 데이터 삭제")
    void clearOnboardData_Success() {
        // Given
        String pattern = "onboarding:" + userId.toString() + ":*";

        // When
        onboardingStateService.clearOnboardingData(userId);

        // Then
        verify(redisTemplate).delete(redisTemplate.keys(pattern));
    }

    @Test
    @DisplayName("온보딩 완료 처리")
    void completeOnboard_Success() {
        // When
        onboardingStateService.completeOnboarding(userId);

        // Then
        String completedKey = "onboarding:" + userId.toString() + ":completed";
        verify(valueOperations).set(completedKey, "true", DEFAULT_TTL);
    }

    @Test
    @DisplayName("온보딩 완료 여부 확인 - 완료됨")
    void isOnboardCompleted_True() {
        // Given
        String completedKey = "onboarding:" + userId.toString() + ":completed";
        when(valueOperations.get(completedKey)).thenReturn("true");

        // When
        boolean result = onboardingStateService.isOnboardingCompleted(userId);

        // Then
        assertThat(result).isTrue();
        verify(valueOperations).get(completedKey);
    }

    @Test
    @DisplayName("온보딩 완료 여부 확인 - 미완료")
    void isOnboardCompleted_False() {
        // Given
        String completedKey = "onboarding:" + userId.toString() + ":completed";
        when(valueOperations.get(completedKey)).thenReturn(null);

        // When
        boolean result = onboardingStateService.isOnboardingCompleted(userId);

        // Then
        assertThat(result).isFalse();
        verify(valueOperations).get(completedKey);
    }
}
