package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.matching.domain.dto.request.MatchingRequestDto;
import com.studymate.domain.matching.entity.MatchingRequest;
import com.studymate.domain.matching.repository.MatchingRequestRepository;
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
class MatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchingRequestRepository matchingRequestRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser1;
    private User testUser2;
    private String accessToken1;
    private String accessToken2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .userId(UUID.randomUUID())
                .name("테스트사용자1")
                .email("test1@example.com")
                .userIdentity("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser2 = User.builder()
                .userId(UUID.randomUUID())
                .name("테스트사용자2")
                .email("test2@example.com")
                .userIdentity("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);

        accessToken1 = jwtUtils.generateAccessToken(testUser1.getUserId());
        accessToken2 = jwtUtils.generateAccessToken(testUser2.getUserId());
    }

    @Test
    @DisplayName("고급 매칭 파트너 검색 통합 테스트")
    void searchAdvancedMatching_Integration_Success() throws Exception {
        // 고급 필터링 요청 생성
        AdvancedMatchingFilterRequest filterRequest = new AdvancedMatchingFilterRequest();
        
        // 실제 DTO 필드에 맞게 설정
        filterRequest.setTargetLanguage("ENGLISH");
        filterRequest.setNativeLanguage("KOREAN");
        filterRequest.setLanguageLevel("INTERMEDIATE");

        // 기타 필터 설정
        filterRequest.setGender("ANY");
        filterRequest.setMinAge(20);
        filterRequest.setMaxAge(40);

        // 기타 필터는 제거 (AdvancedMatchingFilterRequest에 없는 필드)

        mockMvc.perform(post("/api/v1/matching/search")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.partners").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("매칭 요청 보내기 통합 테스트")
    void sendMatchingRequest_Integration_Success() throws Exception {
        MatchingRequestDto requestDto = new MatchingRequestDto();
        requestDto.setTargetUserId(testUser2.getUserId());
        requestDto.setMessage("안녕하세요! 언어 교환을 함께 해요!");
        
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("dayOfWeek", "MONDAY");
        schedule.put("timeSlot", "19:00-21:00");
        requestDto.setPreferredSchedule(schedule);

        mockMvc.perform(post("/api/v1/matching/request")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("받은 매칭 요청 목록 조회 통합 테스트")
    void getReceivedMatchingRequests_Integration_Success() throws Exception {
        // 테스트 매칭 요청 생성
        MatchingRequest matchingRequest = MatchingRequest.builder()
                .fromUser(testUser1)
                .toUser(testUser2)
                .message("테스트 매칭 요청")
                .build();
        matchingRequestRepository.save(matchingRequest);

        mockMvc.perform(get("/api/v1/matching/requests/received")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requests").isArray());
    }

    @Test
    @DisplayName("매칭 요청 응답 통합 테스트")
    void respondToMatchingRequest_Integration_Success() throws Exception {
        // 테스트 매칭 요청 생성
        MatchingRequest matchingRequest = MatchingRequest.builder()
                .fromUser(testUser1)
                .toUser(testUser2)
                .message("테스트 매칭 요청")
                .build();
        matchingRequest = matchingRequestRepository.save(matchingRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("response", "ACCEPTED");
        response.put("message", "네, 좋아요! 함께 공부해요!");

        mockMvc.perform(post("/api/v1/matching/requests/" + matchingRequest.getRequestId() + "/respond")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("매칭 기록 조회 통합 테스트")
    void getMatchingHistory_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/matching/history")
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matches").isArray());
    }

    @Test
    @DisplayName("호환성 점수 계산 통합 테스트")
    void calculateCompatibilityScore_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/matching/compatibility/" + testUser2.getUserId())
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compatibilityScore").isNumber())
                .andExpect(jsonPath("$.data.matchingReasons").isArray());
    }

    @Test
    @DisplayName("자신에게 매칭 요청 시 실패 테스트")
    void sendMatchingRequestToSelf_Integration_BadRequest() throws Exception {
        MatchingRequestDto requestDto = new MatchingRequestDto();
        requestDto.setTargetUserId(testUser1.getUserId()); // 자기 자신에게 요청
        requestDto.setMessage("자기 자신에게 요청");

        mockMvc.perform(post("/api/v1/matching/request")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 매칭 요청 시 실패 테스트")
    void sendMatchingRequestToNonExistentUser_Integration_NotFound() throws Exception {
        MatchingRequestDto requestDto = new MatchingRequestDto();
        requestDto.setTargetUserId(UUID.randomUUID()); // 존재하지 않는 사용자
        requestDto.setMessage("존재하지 않는 사용자에게 요청");

        mockMvc.perform(post("/api/v1/matching/request")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
}