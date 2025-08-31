package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
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
class OnboardingIntegrationTest {

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
                .name("온보딩테스트사용자")
                .email("onboarding@example.com")
                .identityType("NAVER")
                .isOnboardingCompleted(false)
                .build();
        
        testUser = userRepository.save(testUser);
        accessToken = jwtUtils.generateAccessToken(testUser.getUserId());
    }

    @Test
    @DisplayName("온보딩 데이터 조회 통합 테스트")
    void getOnboardingData_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/data")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.languages").isArray())
                .andExpect(jsonPath("$.data.motivations").isArray())
                .andExpect(jsonPath("$.data.learningStyles").isArray())
                .andExpect(jsonPath("$.data.partnerPersonalities").isArray())
                .andExpect(jsonPath("$.data.topics").isArray());
    }

    @Test
    @DisplayName("전체 온보딩 완료 통합 테스트")
    void completeAllOnboarding_Integration_Success() throws Exception {
        CompleteAllOnboardingRequest request = createCompleteOnboardingRequest();

        mockMvc.perform(post("/api/v1/onboarding/complete-all")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 사용자가 온보딩 완료되었는지 확인
        User updatedUser = userRepository.findById(testUser.getUserId()).orElse(null);
        assert updatedUser != null;
        assert updatedUser.getIsOnboardingCompleted();
    }

    @Test
    @DisplayName("언어 레벨 선택 통합 테스트")
    void selectLanguageLevel_Integration_Success() throws Exception {
        Map<String, Object> languageSelection = new HashMap<>();
        languageSelection.put("learningLanguage", "ENGLISH");
        languageSelection.put("nativeLanguage", "KOREAN");
        languageSelection.put("currentLevel", "BEGINNER");
        languageSelection.put("targetLevel", "INTERMEDIATE");

        mockMvc.perform(post("/api/v1/onboarding/language-level")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(languageSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("학습 동기 선택 통합 테스트")
    void selectLearningMotivation_Integration_Success() throws Exception {
        Map<String, Object> motivationSelection = new HashMap<>();
        motivationSelection.put("motivations", Arrays.asList("CAREER", "TRAVEL", "CULTURE"));
        motivationSelection.put("primaryGoal", "CAREER");
        motivationSelection.put("timeCommitment", "3-5_HOURS_WEEK");

        mockMvc.perform(post("/api/v1/onboarding/motivation")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(motivationSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("학습 스타일 선택 통합 테스트")
    void selectLearningStyle_Integration_Success() throws Exception {
        Map<String, Object> styleSelection = new HashMap<>();
        styleSelection.put("learningStyles", Arrays.asList("VISUAL", "AUDITORY"));
        styleSelection.put("preferredPace", "MODERATE");
        styleSelection.put("feedbackPreference", "IMMEDIATE");

        mockMvc.perform(post("/api/v1/onboarding/learning-style")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(styleSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("파트너 선호도 선택 통합 테스트")
    void selectPartnerPreferences_Integration_Success() throws Exception {
        Map<String, Object> partnerPreferences = new HashMap<>();
        partnerPreferences.put("preferredPersonalities", Arrays.asList("PATIENT", "ENCOURAGING"));
        partnerPreferences.put("ageRangeMin", 20);
        partnerPreferences.put("ageRangeMax", 35);
        partnerPreferences.put("genderPreference", "NO_PREFERENCE");
        partnerPreferences.put("communicationStyle", "CASUAL");

        mockMvc.perform(post("/api/v1/onboarding/partner-preference")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partnerPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("학습 주제 선택 통합 테스트")
    void selectLearningTopics_Integration_Success() throws Exception {
        Map<String, Object> topicSelection = new HashMap<>();
        topicSelection.put("topics", Arrays.asList("BUSINESS", "DAILY_LIFE", "TRAVEL"));
        topicSelection.put("primaryInterest", "BUSINESS");
        topicSelection.put("skillFocus", Arrays.asList("SPEAKING", "LISTENING"));

        mockMvc.perform(post("/api/v1/onboarding/topics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("학습 일정 선택 통합 테스트")
    void selectLearningSchedule_Integration_Success() throws Exception {
        Map<String, Object> scheduleSelection = new HashMap<>();
        scheduleSelection.put("preferredDays", Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"));
        scheduleSelection.put("preferredTimes", Arrays.asList("19:00-20:00", "20:00-21:00"));
        scheduleSelection.put("timezone", "Asia/Seoul");
        scheduleSelection.put("sessionLength", "60_MINUTES");
        scheduleSelection.put("frequency", "3_TIMES_WEEK");

        mockMvc.perform(post("/api/v1/onboarding/schedule")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scheduleSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("그룹 크기 선호도 선택 통합 테스트")
    void selectGroupSizePreference_Integration_Success() throws Exception {
        Map<String, Object> groupSizeSelection = new HashMap<>();
        groupSizeSelection.put("preferredSizes", Arrays.asList("ONE_ON_ONE", "SMALL_GROUP"));
        groupSizeSelection.put("primaryPreference", "ONE_ON_ONE");
        groupSizeSelection.put("comfortLevel", "HIGH");

        mockMvc.perform(post("/api/v1/onboarding/group-size")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupSizeSelection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("온보딩 진행 상태 조회 통합 테스트")
    void getOnboardingProgress_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/progress")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completedSteps").isArray())
                .andExpect(jsonPath("$.data.totalSteps").isNumber())
                .andExpect(jsonPath("$.data.progressPercentage").isNumber());
    }

    private CompleteAllOnboardingRequest createCompleteOnboardingRequest() {
        CompleteAllOnboardingRequest request = new CompleteAllOnboardingRequest();
        
        // 언어 설정
        request.setLearningLanguage("ENGLISH");
        request.setNativeLanguage("KOREAN");
        request.setCurrentLevel("BEGINNER");
        request.setTargetLevel("INTERMEDIATE");

        // 학습 동기
        request.setMotivations(Arrays.asList("CAREER", "TRAVEL"));
        request.setPrimaryGoal("CAREER");
        request.setTimeCommitment("3-5_HOURS_WEEK");

        // 학습 스타일
        request.setLearningStyles(Arrays.asList("VISUAL", "AUDITORY"));
        request.setPreferredPace("MODERATE");
        request.setFeedbackPreference("IMMEDIATE");

        // 파트너 선호도
        request.setPreferredPersonalities(Arrays.asList("PATIENT", "ENCOURAGING"));
        request.setAgeRangeMin(20);
        request.setAgeRangeMax(35);
        request.setGenderPreference("NO_PREFERENCE");
        request.setCommunicationStyle("CASUAL");

        // 학습 주제
        request.setTopics(Arrays.asList("BUSINESS", "DAILY_LIFE"));
        request.setPrimaryInterest("BUSINESS");
        request.setSkillFocus(Arrays.asList("SPEAKING", "LISTENING"));

        // 학습 일정
        request.setPreferredDays(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"));
        request.setPreferredTimes(Arrays.asList("19:00-20:00", "20:00-21:00"));
        request.setTimezone("Asia/Seoul");
        request.setSessionLength("60_MINUTES");
        request.setFrequency("3_TIMES_WEEK");

        // 그룹 크기
        request.setPreferredSizes(Arrays.asList("ONE_ON_ONE", "SMALL_GROUP"));
        request.setPrimaryPreference("ONE_ON_ONE");
        request.setComfortLevel("HIGH");

        return request;
    }
}