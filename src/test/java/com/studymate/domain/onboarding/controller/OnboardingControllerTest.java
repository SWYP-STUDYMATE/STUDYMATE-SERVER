package com.studymate.domain.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.onboarding.dto.response.OnboardingStatusResponse;
import com.studymate.domain.onboarding.service.OnboardingStateService;
import com.studymate.auth.jwt.JwtAuthenticationFilter;
import com.studymate.auth.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OnboardingController.class)
@DisplayName("OnboardingController 통합 테스트")
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OnboardingStateService onboardingStateService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(jwtUtils.getUserIdFromToken(anyString())).thenReturn(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 단계 데이터 저장 성공")
    void saveStepData_Success() throws Exception {
        // Given
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("interests", "programming,music");
        stepData.put("level", "INTERMEDIATE");

        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/step/1")
                .with(csrf())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stepData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("1단계 데이터가 저장되었습니다."));

        verify(onboardingStateService).saveStepData(eq(userId), eq(1), any(Map.class));
        verify(onboardingStateService).setCurrentStep(userId, 2);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 단계 데이터 조회 성공")
    void getStepData_Success() throws Exception {
        // Given
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("interests", "programming,music");
        stepData.put("level", "INTERMEDIATE");
        
        when(onboardingStateService.getStepData(userId, 1)).thenReturn(stepData);

        // When & Then
        mockMvc.perform(get("/api/v1/onboarding/step/1")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.interests").value("programming,music"))
                .andExpect(jsonPath("$.data.level").value("INTERMEDIATE"));

        verify(onboardingStateService).getStepData(userId, 1);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 현재 단계 조회 성공")
    void getCurrentStep_Success() throws Exception {
        // Given
        when(onboardingStateService.getCurrentStep(userId)).thenReturn(3);

        // When & Then
        mockMvc.perform(get("/api/v1/onboarding/current-step")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStep").value(3));

        verify(onboardingStateService).getCurrentStep(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 단계 건너뛰기 성공")
    void skipStep_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/step/2/skip")
                .with(csrf())
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("2단계를 건너뛰었습니다."));

        verify(onboardingStateService).skipStep(userId, 2);
        verify(onboardingStateService).setCurrentStep(userId, 3);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 상태 조회 성공")
    void getOnboardingStatus_Success() throws Exception {
        // Given
        OnboardingStatusResponse statusResponse = OnboardingStatusResponse.builder()
                .currentStep(3)
                .totalSteps(8)
                .progressPercentage(37.5)
                .completed(false)
                .build();
        
        when(onboardingStateService.getOnboardingStatus(userId)).thenReturn(statusResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/onboarding/status")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStep").value(3))
                .andExpected(jsonPath("$.data.totalSteps").value(8))
                .andExpected(jsonPath("$.data.progressPercentage").value(37.5))
                .andExpected(jsonPath("$.data.completed").value(false));

        verify(onboardingStateService).getOnboardingStatus(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 자동 저장 성공")
    void autoSave_Success() throws Exception {
        // Given
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("temp_data", "임시 저장 데이터");

        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/auto-save")
                .with(csrf())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stepData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("자동 저장되었습니다."));

        verify(onboardingStateService).saveStepData(eq(userId), eq(0), any(Map.class));
    }

    @Test
    @WithMockUser
    @DisplayName("체험 매칭 요청 성공")
    void requestTrialMatching_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/trial-matching")
                .with(csrf())
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("체험 매칭이 요청되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("온보딩 진행률 조회 성공")
    void getProgress_Success() throws Exception {
        // Given
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("currentStep", 4);
        progressData.put("totalSteps", 8);
        progressData.put("progressPercentage", 50.0);
        
        when(onboardingStateService.getOnboardingStatus(userId)).thenReturn(
                OnboardingStatusResponse.builder()
                        .currentStep(4)
                        .totalSteps(8)
                        .progressPercentage(50.0)
                        .completed(false)
                        .build()
        );

        // When & Then
        mockMvc.perform(get("/api/v1/onboarding/progress")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.progressPercentage").value(50.0));

        verify(onboardingStateService).getOnboardingStatus(userId);
    }

    @Test
    @DisplayName("인증되지 않은 요청 실패")
    void unauthenticatedRequest_Failure() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/step/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("잘못된 단계 번호로 요청 실패")
    void invalidStepNumber_Failure() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/step/0")
                .with(csrf())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("빈 요청 본문으로 저장 실패")
    void emptyRequestBody_Failure() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/onboarding/step/1")
                .with(csrf())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }
}