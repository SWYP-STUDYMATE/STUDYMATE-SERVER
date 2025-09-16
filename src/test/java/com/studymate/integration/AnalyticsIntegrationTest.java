package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.auth.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(UUID.randomUUID())
                .name("분석테스트사용자")
                .email("analytics@example.com")
                .userIdentity("NAVER")
                .isOnboardingCompleted(true)
                .build();
        
        testUser = userRepository.save(testUser);
        accessToken = jwtUtils.generateAccessToken(testUser.getUserId());
    }

    @Test
    @DisplayName("실시간 대시보드 통합 테스트")
    void getRealtimeDashboard_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard/realtime")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeUsers").isNumber())
                .andExpect(jsonPath("$.data.ongoingSessions").isNumber())
                .andExpect(jsonPath("$.data.totalMessages").isNumber());
    }

    @Test
    @DisplayName("사용자 활동 통계 통합 테스트")
    void getUserActivityStats_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/user-activity")
                .header("Authorization", "Bearer " + accessToken)
                .param("period", "WEEKLY")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyActiveUsers").isArray())
                .andExpect(jsonPath("$.data.totalSessions").isNumber())
                .andExpect(jsonPath("$.data.averageSessionDuration").isNumber());
    }

    @Test
    @DisplayName("매칭 성공률 분석 통합 테스트")
    void getMatchingSuccessRate_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/matching-success-rate")
                .header("Authorization", "Bearer " + accessToken)
                .param("period", "MONTHLY")
                .param("languagePair", "KOREAN-ENGLISH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successRate").isNumber())
                .andExpect(jsonPath("$.data.totalRequests").isNumber())
                .andExpect(jsonPath("$.data.successfulMatches").isNumber());
    }

    @Test
    @DisplayName("언어별 사용자 분포 통합 테스트")
    void getLanguageDistribution_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/language-distribution")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.learningLanguages").isObject())
                .andExpect(jsonPath("$.data.nativeLanguages").isObject())
                .andExpect(jsonPath("$.data.popularPairs").isArray());
    }

    @Test
    @DisplayName("세션 품질 지표 통합 테스트")
    void getSessionQualityMetrics_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/session-quality")
                .header("Authorization", "Bearer " + accessToken)
                .param("period", "DAILY")
                .param("minDuration", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageRating").isNumber())
                .andExpect(jsonPath("$.data.completionRate").isNumber())
                .andExpect(jsonPath("$.data.averageDuration").isNumber());
    }

    @Test
    @DisplayName("사용자 참여도 분석 통합 테스트")
    void getUserEngagementAnalysis_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/user-engagement")
                .header("Authorization", "Bearer " + accessToken)
                .param("cohort", "2024-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.retentionRates").isObject())
                .andExpect(jsonPath("$.data.engagementScore").isNumber())
                .andExpect(jsonPath("$.data.churnRate").isNumber());
    }

    @Test
    @DisplayName("실시간 알림 통합 테스트")
    void getRealtimeAlerts_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/alerts")
                .header("Authorization", "Bearer " + accessToken)
                .param("severity", "HIGH")
                .param("category", "PERFORMANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alerts").isArray())
                .andExpect(jsonPath("$.data.totalCount").isNumber());
    }

    @Test
    @DisplayName("비인가 접근 시 실패 테스트")
    void accessWithoutAuth_Integration_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard/realtime"))
                .andExpect(status().isUnauthorized());
    }
}