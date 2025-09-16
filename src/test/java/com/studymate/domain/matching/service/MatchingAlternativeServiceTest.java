package com.studymate.domain.matching.service;

import com.studymate.domain.matching.dto.request.MatchingAlternativeRequest;
import com.studymate.domain.matching.dto.response.MatchingAlternativeResponse;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingAlternativeService 단위 테스트")
class MatchingAlternativeServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchingAlternativeService matchingAlternativeService;

    private UUID userId;
    private User user;
    private MatchingAlternativeRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        user = User.builder()
                .userId(userId)
                .username("testUser")
                .nativeLanguage("KOREAN")
                .targetLanguage("ENGLISH")
                .englishLevel("INTERMEDIATE")
                .preferredGender("ANY")
                .personalityType("EXTROVERT")
                .communicationStyle("FORMAL")
                .build();
        
        request = MatchingAlternativeRequest.builder()
                .userId(userId)
                .failureReason("NO_AVAILABLE_PARTNERS")
                .searchDuration(300)
                .attemptCount(3)
                .lastAttemptTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("매칭 대안 분석 - 사용 가능한 파트너 없음")
    void analyzeAlternatives_NoAvailablePartners() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setFailureReason("NO_AVAILABLE_PARTNERS");

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("GROUP_SESSION"));
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("AI_PARTNER"));
        assertThat(response.getRecommendedAlternative()).isEqualTo("GROUP_SESSION");
    }

    @Test
    @DisplayName("매칭 대안 분석 - 언어 레벨 불일치")
    void analyzeAlternatives_LanguageLevelMismatch() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setFailureReason("LANGUAGE_LEVEL_MISMATCH");

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("RELAXED_CRITERIA"));
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("AI_PARTNER"));
        assertThat(response.getRecommendedAlternative()).isEqualTo("AI_PARTNER");
    }

    @Test
    @DisplayName("매칭 대안 분석 - 선호도 불일치")
    void analyzeAlternatives_PreferenceMismatch() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setFailureReason("PREFERENCE_MISMATCH");

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("RELAXED_CRITERIA"));
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("GROUP_SESSION"));
    }

    @Test
    @DisplayName("매칭 대안 분석 - 시간대 불일치")
    void analyzeAlternatives_TimeMismatch() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setFailureReason("TIME_MISMATCH");

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("ASYNCHRONOUS"));
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("FLEXIBLE_SCHEDULE"));
        assertThat(response.getRecommendedAlternative()).isEqualTo("FLEXIBLE_SCHEDULE");
    }

    @Test
    @DisplayName("매칭 대안 분석 - 다중 시도 후 실패")
    void analyzeAlternatives_MultipleAttempts() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setAttemptCount(5);
        request.setSearchDuration(600);

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("AI_PARTNER"));
        assertThat(response.getRecommendedAlternative()).isEqualTo("AI_PARTNER");
        assertThat(response.getReason()).contains("여러 번의 매칭 시도");
    }

    @Test
    @DisplayName("매칭 대안 분석 - 긴 검색 시간")
    void analyzeAlternatives_LongSearchDuration() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setSearchDuration(900); // 15분

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("GROUP_SESSION"));
        assertThat(response.getRecommendedAlternative()).isEqualTo("GROUP_SESSION");
    }

    @Test
    @DisplayName("대안 우선순위 계산 - AI 파트너")
    void calculateAlternativePriority_AiPartner() {
        // Given
        request.setFailureReason("LANGUAGE_LEVEL_MISMATCH");
        request.setAttemptCount(4);

        // When
        int priority = matchingAlternativeService.calculateAlternativePriority("AI_PARTNER", request);

        // Then
        assertThat(priority).isGreaterThan(0);
    }

    @Test
    @DisplayName("대안 우선순위 계산 - 그룹 세션")
    void calculateAlternativePriority_GroupSession() {
        // Given
        request.setFailureReason("NO_AVAILABLE_PARTNERS");
        request.setSearchDuration(600);

        // When
        int priority = matchingAlternativeService.calculateAlternativePriority("GROUP_SESSION", request);

        // Then
        assertThat(priority).isGreaterThan(0);
    }

    @Test
    @DisplayName("추천 이유 생성")
    void generateRecommendationReason() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response.getReason()).isNotEmpty();
        assertThat(response.getReason()).contains("매칭");
    }

    @Test
    @DisplayName("사용자 맞춤 대안 제안")
    void generatePersonalizedAlternatives() {
        // Given
        user.setEnglishLevel("BEGINNER");
        user.setPersonalityType("INTROVERT");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAlternatives()).isNotEmpty();
        // 초보자와 내향적인 사용자에게는 AI 파트너가 더 적절할 수 있음
        assertThat(response.getAlternatives()).anyMatch(alt -> alt.getType().equals("AI_PARTNER"));
    }

    @Test
    @DisplayName("예상 대기 시간 계산")
    void calculateEstimatedWaitTime() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        request.setFailureReason("NO_AVAILABLE_PARTNERS");

        // When
        MatchingAlternativeResponse response = matchingAlternativeService.analyzeAlternatives(request);

        // Then
        assertThat(response.getEstimatedWaitTime()).isNotNull();
        assertThat(response.getEstimatedWaitTime()).isGreaterThan(0);
    }
}