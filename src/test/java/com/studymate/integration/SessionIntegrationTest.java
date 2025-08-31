package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.session.domain.dto.request.SessionCreateRequest;
import com.studymate.domain.session.domain.dto.request.SessionJoinRequest;
import com.studymate.domain.session.domain.dto.request.SessionFeedbackRequest;
import com.studymate.domain.session.entity.Session;
import com.studymate.domain.session.repository.SessionRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser1;
    private User testUser2;
    private String accessToken1;
    private String accessToken2;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .userId(UUID.randomUUID())
                .name("세션테스트사용자1")
                .email("session1@example.com")
                .identityType("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser2 = User.builder()
                .userId(UUID.randomUUID())
                .name("세션테스트사용자2")
                .email("session2@example.com")
                .identityType("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);

        accessToken1 = jwtUtils.generateAccessToken(testUser1.getUserId());
        accessToken2 = jwtUtils.generateAccessToken(testUser2.getUserId());

        testSession = Session.builder()
                .title("테스트 세션")
                .description("통합 테스트를 위한 세션")
                .hostUser(testUser1)
                .targetLanguage("ENGLISH")
                .sessionType("VIDEO_CHAT")
                .maxParticipants(2)
                .scheduledStartTime(LocalDateTime.now().plusHours(1))
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("세션 생성 통합 테스트")
    void createSession_Integration_Success() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setTitle("새로운 영어 세션");
        request.setDescription("영어 회화 연습 세션");
        request.setTargetLanguage("ENGLISH");
        request.setSessionType("VIDEO_CHAT");
        request.setMaxParticipants(4);
        request.setScheduledStartTime(LocalDateTime.now().plusHours(2));
        request.setTopics(Arrays.asList("DAILY_LIFE", "BUSINESS"));

        mockMvc.perform(post("/api/v1/sessions")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("새로운 영어 세션"));
    }

    @Test
    @DisplayName("세션 목록 조회 통합 테스트")
    void getSessionList_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/sessions")
                .header("Authorization", "Bearer " + accessToken1)
                .param("language", "ENGLISH")
                .param("type", "VIDEO_CHAT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("세션 참가 통합 테스트")
    void joinSession_Integration_Success() throws Exception {
        SessionJoinRequest request = new SessionJoinRequest();
        request.setMessage("참가하고 싶습니다!");
        
        Map<String, String> userLanguageInfo = new HashMap<>();
        userLanguageInfo.put("nativeLanguage", "KOREAN");
        userLanguageInfo.put("learningLanguage", "ENGLISH");
        userLanguageInfo.put("level", "INTERMEDIATE");
        request.setUserLanguageInfo(userLanguageInfo);

        mockMvc.perform(post("/api/v1/sessions/" + testSession.getSessionId() + "/join")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("세션 상세 정보 조회 통합 테스트")
    void getSessionDetail_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/" + testSession.getSessionId())
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("테스트 세션"))
                .andExpect(jsonPath("$.data.participants").isArray());
    }

    @Test
    @DisplayName("세션 시작 통합 테스트")
    void startSession_Integration_Success() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/" + testSession.getSessionId() + "/start")
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("세션 종료 통합 테스트")
    void endSession_Integration_Success() throws Exception {
        Map<String, Object> endRequest = new HashMap<>();
        endRequest.put("reason", "COMPLETED");
        endRequest.put("summary", "성공적인 세션이었습니다.");

        mockMvc.perform(post("/api/v1/sessions/" + testSession.getSessionId() + "/end")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("세션 피드백 제출 통합 테스트")
    void submitSessionFeedback_Integration_Success() throws Exception {
        SessionFeedbackRequest request = new SessionFeedbackRequest();
        request.setRating(5);
        request.setComment("훌륭한 세션이었습니다!");
        request.setLanguageExchangeQuality(4);
        request.setCommunicationEffectiveness(5);
        request.setTechnicalQuality(4);
        request.setRecommendToOthers(true);

        mockMvc.perform(post("/api/v1/sessions/" + testSession.getSessionId() + "/feedback")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("내 세션 기록 조회 통합 테스트")
    void getMySessionHistory_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/my-history")
                .header("Authorization", "Bearer " + accessToken1)
                .param("status", "COMPLETED")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("세션 검색 통합 테스트")
    void searchSessions_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/search")
                .header("Authorization", "Bearer " + accessToken1)
                .param("query", "영어")
                .param("language", "ENGLISH")
                .param("level", "INTERMEDIATE")
                .param("sortBy", "SCHEDULED_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions").isArray());
    }

    @Test
    @DisplayName("권한 없는 세션 조작 시 실패 테스트")
    void unauthorizedSessionAccess_Integration_Forbidden() throws Exception {
        UUID unauthorizedSessionId = UUID.randomUUID();
        
        mockMvc.perform(post("/api/v1/sessions/" + unauthorizedSessionId + "/start")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isForbidden());
    }
}