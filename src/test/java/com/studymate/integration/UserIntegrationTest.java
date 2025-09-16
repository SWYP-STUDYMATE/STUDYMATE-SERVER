package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.response.OnlineStatusResponse;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.auth.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class UserIntegrationTest {

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
                .name("테스트사용자")
                .email("test@example.com")
                .userIdentity("NAVER")
                .isOnboardingCompleted(true)
                .build();
        
        testUser = userRepository.save(testUser);
        accessToken = jwtUtils.generateAccessToken(testUser.getUserId());
    }

    @Test
    @DisplayName("사용자 프로필 정보 조회 통합 테스트")
    void getUserProfile_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/user/name")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트사용자"));
    }

    @Test
    @DisplayName("영어 이름 저장 통합 테스트")
    void saveEnglishName_Integration_Success() throws Exception {
        EnglishNameRequest request = new EnglishNameRequest("John Doe");

        mockMvc.perform(post("/api/v1/user/english-name")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 데이터베이스 검증
        User updatedUser = userRepository.findById(testUser.getUserId()).orElse(null);
        assert updatedUser != null;
        assert "John Doe".equals(updatedUser.getEnglishName());
    }

    @Test
    @DisplayName("사용자 온라인 상태 조회 통합 테스트")
    void getUserOnlineStatus_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/user/status/" + testUser.getUserId())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testUser.getUserId().toString()));
    }

    @Test
    @DisplayName("사용자 온라인 상태 업데이트 통합 테스트")
    void updateUserOnlineStatus_Integration_Success() throws Exception {
        mockMvc.perform(post("/api/v1/user/status/update")
                .header("Authorization", "Bearer " + accessToken)
                .param("status", "ONLINE")
                .param("deviceInfo", "Chrome on MacOS")
                .param("activity", "STUDYING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("위치 정보 저장 통합 테스트")
    void saveLocation_Integration_Success() throws Exception {
        LocationRequest request = new LocationRequest(1);

        mockMvc.perform(post("/api/v1/user/location")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("인증 실패 테스트")
    void accessWithoutToken_Integration_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/name"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 토큰으로 접근 시 실패 테스트")
    void accessWithInvalidToken_Integration_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/name")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}