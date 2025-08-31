package com.studymate.domain.matching.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.matching.dto.RelaxedCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingAlternativeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 패턴
    private static final String MATCHING_ANALYSIS_PREFIX = "matching:analysis:";
    private static final String ALTERNATIVE_CACHE_PREFIX = "matching:alternatives:";
    private static final String USER_PREFERENCES_PREFIX = "matching:preferences:";
    private static final String MATCHING_HISTORY_PREFIX = "matching:history:";
    
    // 캐시 및 분석 설정
    private static final Duration ANALYSIS_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration ALTERNATIVE_CACHE_TTL = Duration.ofHours(2);
    private static final Duration PREFERENCES_TTL = Duration.ofDays(7);

    /**
     * 매칭 실패 시 대안 분석 및 제안
     */
    public MatchingAlternativeResponse analyzeMatchingFailure(UUID userId, MatchingFailureContext context) {
        try {
            log.info("Analyzing matching failure for user {}: reason={}", userId, context.getFailureReason());
            
            // 매칭 실패 분석
            FailureAnalysis analysis = analyzeFailureReasons(userId, context);
            
            // 대안 생성
            List<MatchingAlternative> alternatives = generateAlternatives(userId, analysis);
            
            // 사용자 맞춤 권장사항
            List<String> recommendations = generatePersonalizedRecommendations(userId, analysis);
            
            // 향후 매칭 성공 확률 예측
            MatchingSuccessPrediction prediction = predictMatchingSuccess(userId, analysis);
            
            MatchingAlternativeResponse response = MatchingAlternativeResponse.builder()
                    .userId(userId)
                    .failureAnalysis(analysis)
                    .alternatives(alternatives)
                    .recommendations(recommendations)
                    .successPrediction(prediction)
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            // 결과 캐시
            cacheAlternativeResponse(userId, response);
            
            // 사용자 선호도 업데이트
            updateUserPreferences(userId, context);
            
            log.info("Generated {} alternatives for user {}", alternatives.size(), userId);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to analyze matching failure for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("매칭 대안 분석에 실패했습니다.", e);
        }
    }

    /**
     * 그룹 세션 대안 제안
     */
    public List<GroupSessionAlternative> suggestGroupSessions(UUID userId, MatchingPreferences preferences) {
        try {
            List<GroupSessionAlternative> groupAlternatives = new ArrayList<>();
            
            // 1. 언어 교환 그룹 세션
            groupAlternatives.addAll(findLanguageExchangeGroups(userId, preferences));
            
            // 2. 주제별 그룹 스터디
            groupAlternatives.addAll(findTopicBasedGroups(userId, preferences));
            
            // 3. 수준별 그룹 세션
            groupAlternatives.addAll(findLevelBasedGroups(userId, preferences));
            
            // 4. 시간대별 그룹 매칭
            groupAlternatives.addAll(findTimeBasedGroups(userId, preferences));
            
            // 관련성 및 품질 점수로 정렬
            return groupAlternatives.stream()
                    .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                    .limit(10)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Failed to suggest group sessions for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * AI 연습 파트너 추천
     */
    public List<AiPartnerRecommendation> recommendAiPartners(UUID userId, MatchingPreferences preferences) {
        try {
            List<AiPartnerRecommendation> aiPartners = new ArrayList<>();
            
            // 사용자 언어 수준에 따른 AI 파트너 추천
            LanguageLevel userLevel = preferences.getCurrentLanguageLevel();
            
            // 1. 기초 회화 AI 파트너
            if (userLevel == LanguageLevel.BEGINNER || userLevel == LanguageLevel.ELEMENTARY) {
                aiPartners.add(createBasicConversationAi(preferences));
            }
            
            // 2. 중급 토론 AI 파트너  
            if (userLevel == LanguageLevel.INTERMEDIATE || userLevel == LanguageLevel.UPPER_INTERMEDIATE) {
                aiPartners.add(createDiscussionAi(preferences));
            }
            
            // 3. 고급 비즈니스 AI 파트너
            if (userLevel == LanguageLevel.ADVANCED || userLevel == LanguageLevel.PROFICIENT) {
                aiPartners.add(createBusinessAi(preferences));
            }
            
            // 4. 특수 목적 AI 파트너들
            aiPartners.addAll(createSpecializedAiPartners(userId, preferences));
            
            return aiPartners.stream()
                    .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Failed to recommend AI partners for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 매칭 기준 완화 제안
     */
    public RelaxedMatchingOptions suggestRelaxedCriteria(UUID userId, MatchingPreferences originalPreferences) {
        try {
            RelaxedCriteria relaxedCriteria = analyzeRelaxableCriteria(userId, originalPreferences);
            
            // 완화 옵션별 예상 매칭 확률 계산
            Map<String, Double> probabilityImprovements = new HashMap<>();
            
            // TODO: RelaxedCriteria Lombok 이슈로 임시 비활성화
            // 연령대 범위 확대
            probabilityImprovements.put("AGE_RANGE", 0.15);
            
            // 언어 수준 범위 확대  
            probabilityImprovements.put("LANGUAGE_LEVEL", 0.12);
            
            // 지역 범위 확대
            probabilityImprovements.put("LOCATION", 0.20);
            
            // 시간대 유연성 증가
            probabilityImprovements.put("TIME_SLOTS", 0.10);
            
            RelaxedMatchingOptions options = RelaxedMatchingOptions.builder()
                    .userId(userId)
                    .originalPreferences(originalPreferences)
                    .relaxedCriteria(relaxedCriteria)
                    .probabilityImprovements(probabilityImprovements)
                    .estimatedWaitTimeReduction(calculateWaitTimeReduction(probabilityImprovements))
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            return options;
            
        } catch (Exception e) {
            log.error("Failed to suggest relaxed criteria for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("매칭 기준 완화 제안에 실패했습니다.", e);
        }
    }

    /**
     * 대기 시간 기반 동적 대안 제공
     */
    public DynamicAlternativeResponse provideDynamicAlternatives(UUID userId, long waitTimeMinutes) {
        try {
            List<DynamicAlternative> alternatives = new ArrayList<>();
            
            if (waitTimeMinutes >= 5) {
                // 5분 이상 대기 시 그룹 세션 제안
                alternatives.add(DynamicAlternative.builder()
                        .type(AlternativeType.GROUP_SESSION)
                        .title("그룹 언어교환에 참여하기")
                        .description("지금 바로 시작할 수 있는 그룹 세션이 있어요")
                        .estimatedWaitTime(Duration.ofMinutes(2))
                        .priority(AlternativePriority.HIGH)
                        .build());
            }
            
            if (waitTimeMinutes >= 10) {
                // 10분 이상 대기 시 AI 파트너 제안
                alternatives.add(DynamicAlternative.builder()
                        .type(AlternativeType.AI_PARTNER)
                        .title("AI 파트너와 연습하기")
                        .description("실제 매칭을 기다리는 동안 AI와 대화 연습을 해보세요")
                        .estimatedWaitTime(Duration.ofSeconds(30))
                        .priority(AlternativePriority.MEDIUM)
                        .build());
            }
            
            if (waitTimeMinutes >= 15) {
                // 15분 이상 대기 시 기준 완화 제안
                alternatives.add(DynamicAlternative.builder()
                        .type(AlternativeType.RELAXED_CRITERIA)
                        .title("매칭 조건 완화하기")
                        .description("조건을 조금 완화하면 더 빨리 매칭될 수 있어요")
                        .estimatedWaitTime(Duration.ofMinutes(5))
                        .priority(AlternativePriority.HIGH)
                        .build());
            }
            
            return DynamicAlternativeResponse.builder()
                    .userId(userId)
                    .currentWaitTime(Duration.ofMinutes(waitTimeMinutes))
                    .alternatives(alternatives)
                    .nextCheckIn(LocalDateTime.now().plusMinutes(5))
                    .generatedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to provide dynamic alternatives for user {}: {}", userId, e.getMessage());
            return DynamicAlternativeResponse.builder()
                    .userId(userId)
                    .alternatives(Collections.emptyList())
                    .build();
        }
    }

    /**
     * 매칭 성공률 향상 제안
     */
    public MatchingImprovementSuggestions suggestImprovements(UUID userId) {
        try {
            // 사용자 매칭 히스토리 분석
            MatchingHistory history = getUserMatchingHistory(userId);
            
            List<ImprovementSuggestion> suggestions = new ArrayList<>();
            
            // 프로필 완성도 체크
            ProfileCompleteness completeness = analyzeProfileCompleteness(userId);
            if (completeness.getScore() < 0.8) {
                suggestions.add(ImprovementSuggestion.builder()
                        .type(ImprovementType.PROFILE_COMPLETION)
                        .title("프로필을 더 자세히 작성해보세요")
                        .description("프로필이 상세할수록 더 좋은 매칭을 받을 수 있어요")
                        .impact(ImprovementImpact.HIGH)
                        .effort(EffortLevel.LOW)
                        .build());
            }
            
            // 활동 시간대 최적화
            TimeSlotAnalysis timeAnalysis = analyzeOptimalTimeSlots(userId);
            if (!timeAnalysis.isOptimal()) {
                suggestions.add(ImprovementSuggestion.builder()
                        .type(ImprovementType.TIME_OPTIMIZATION)
                        .title("활동 시간대를 조정해보세요")
                        .description(String.format("%s에 활동하면 매칭 확률이 %d%% 높아져요", 
                            timeAnalysis.getOptimalTimeSlot(), (int)(timeAnalysis.getImprovement() * 100)))
                        .impact(ImprovementImpact.MEDIUM)
                        .effort(EffortLevel.LOW)
                        .build());
            }
            
            // 언어 수준 재평가
            if (history.getFailureRate() > 0.3) {
                suggestions.add(ImprovementSuggestion.builder()
                        .type(ImprovementType.LANGUAGE_LEVEL_REASSESSMENT)
                        .title("언어 수준을 재평가해보세요")
                        .description("현재 실력에 맞는 수준으로 조정하면 더 적합한 파트너를 만날 수 있어요")
                        .impact(ImprovementImpact.HIGH)
                        .effort(EffortLevel.MEDIUM)
                        .build());
            }
            
            return MatchingImprovementSuggestions.builder()
                    .userId(userId)
                    .currentSuccessRate(history.getSuccessRate())
                    .suggestions(suggestions)
                    .potentialSuccessRate(calculatePotentialSuccessRate(history, suggestions))
                    .generatedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to suggest improvements for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("매칭 개선 제안에 실패했습니다.", e);
        }
    }

    // === Private Helper Methods ===
    
    private FailureAnalysis analyzeFailureReasons(UUID userId, MatchingFailureContext context) {
        FailureAnalysis.FailureAnalysisBuilder analysisBuilder = FailureAnalysis.builder()
                .primaryReason(context.getFailureReason())
                .analysisTime(LocalDateTime.now());
        
        // 실패 원인별 상세 분석
        switch (context.getFailureReason()) {
            case NO_AVAILABLE_USERS:
                analysisBuilder.contributingFactors(Arrays.asList(
                    "현재 시간대에 활성 사용자 부족",
                    "언어 수준 매칭 조건이 너무 엄격",
                    "지역 제한이 너무 좁음"
                ));
                break;
            case CRITERIA_TOO_STRICT:
                analysisBuilder.contributingFactors(Arrays.asList(
                    "연령대 범위가 너무 제한적",
                    "관심사 매칭 조건이 너무 구체적",
                    "시간대 유연성 부족"
                ));
                break;
            case PEAK_TIME_CONGESTION:
                analysisBuilder.contributingFactors(Arrays.asList(
                    "피크 시간대 경쟁 심화",
                    "대기열에서 우선순위 낮음",
                    "매칭 알고리즘 성능 저하"
                ));
                break;
            default:
                analysisBuilder.contributingFactors(Collections.emptyList());
        }
        
        return analysisBuilder.build();
    }
    
    private List<MatchingAlternative> generateAlternatives(UUID userId, FailureAnalysis analysis) {
        List<MatchingAlternative> alternatives = new ArrayList<>();
        
        // 1. 그룹 세션 대안
        alternatives.add(MatchingAlternative.builder()
                .type(AlternativeType.GROUP_SESSION)
                .title("그룹 언어교환")
                .description("3-5명과 함께하는 그룹 언어교환 세션")
                .estimatedWaitTime(Duration.ofMinutes(3))
                .successProbability(0.85)
                .benefits(Arrays.asList("즉시 시작 가능", "다양한 관점 학습", "부담 없는 분위기"))
                .priority(AlternativePriority.HIGH)
                .build());
        
        // 2. AI 파트너 대안
        alternatives.add(MatchingAlternative.builder()
                .type(AlternativeType.AI_PARTNER)
                .title("AI 대화 파트너")
                .description("개인 맞춤형 AI와 1:1 대화 연습")
                .estimatedWaitTime(Duration.ofSeconds(30))
                .successProbability(1.0)
                .benefits(Arrays.asList("24시간 이용 가능", "실수해도 부담 없음", "맞춤형 피드백"))
                .priority(AlternativePriority.MEDIUM)
                .build());
        
        // 3. 기준 완화 대안
        alternatives.add(MatchingAlternative.builder()
                .type(AlternativeType.RELAXED_CRITERIA)
                .title("매칭 조건 완화")
                .description("조건을 완화하여 더 많은 파트너와 매칭")
                .estimatedWaitTime(Duration.ofMinutes(5))
                .successProbability(0.75)
                .benefits(Arrays.asList("매칭 확률 증가", "새로운 경험", "유연성 향상"))
                .priority(AlternativePriority.MEDIUM)
                .build());
        
        // 실패 원인에 따른 맞춤 대안 추가
        if (analysis.getPrimaryReason() == MatchingFailureReason.NO_AVAILABLE_USERS) {
            alternatives.add(MatchingAlternative.builder()
                    .type(AlternativeType.SCHEDULED_SESSION)
                    .title("예약 세션")
                    .description("인기 시간대에 미리 예약하기")
                    .estimatedWaitTime(Duration.ofHours(2))
                    .successProbability(0.95)
                    .benefits(Arrays.asList("확실한 매칭 보장", "원하는 시간 선택", "준비할 수 있는 시간"))
                    .priority(AlternativePriority.LOW)
                    .build());
        }
        
        return alternatives;
    }
    
    private List<String> generatePersonalizedRecommendations(UUID userId, FailureAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("오후 7-9시에 접속하면 더 많은 사용자를 만날 수 있어요");
        recommendations.add("프로필에 관심사를 더 자세히 작성해보세요");
        recommendations.add("언어 수준을 한 단계 낮춰보면 더 쉽게 매칭될 수 있어요");
        recommendations.add("주말보다는 평일 저녁이 매칭하기 좋은 시간이에요");
        
        return recommendations;
    }
    
    private MatchingSuccessPrediction predictMatchingSuccess(UUID userId, FailureAnalysis analysis) {
        // 간단한 예측 모델 (실제로는 ML 모델 사용)
        double baseProbability = 0.6;
        
        // 시간대별 가중치
        double timeWeight = isOptimalTime() ? 1.2 : 0.8;
        
        // 사용자 활동 이력 가중치
        double activityWeight = getUserActivityScore(userId);
        
        double predictedProbability = baseProbability * timeWeight * activityWeight;
        predictedProbability = Math.min(1.0, predictedProbability);
        
        return MatchingSuccessPrediction.builder()
                .probabilityWithinHour(predictedProbability)
                .probabilityWithinDay(Math.min(1.0, predictedProbability * 1.5))
                .optimalRetryTime(LocalDateTime.now().plusHours(2))
                .confidenceLevel(0.7)
                .build();
    }
    
    private List<GroupSessionAlternative> findLanguageExchangeGroups(UUID userId, MatchingPreferences preferences) {
        // 실제로는 데이터베이스에서 조회
        return Arrays.asList(
            GroupSessionAlternative.builder()
                    .sessionId("lang-exchange-001")
                    .title("영어-한국어 교환 그룹")
                    .description("영어와 한국어를 교환하는 친근한 그룹입니다")
                    .currentParticipants(3)
                    .maxParticipants(6)
                    .averageLevel(preferences.getCurrentLanguageLevel())
                    .compatibilityScore(0.85)
                    .startTime(LocalDateTime.now().plusMinutes(10))
                    .build()
        );
    }
    
    private List<GroupSessionAlternative> findTopicBasedGroups(UUID userId, MatchingPreferences preferences) {
        return Arrays.asList(
            GroupSessionAlternative.builder()
                    .sessionId("topic-tech-001")
                    .title("기술 트렌드 토론 그룹")
                    .description("최신 기술에 대해 영어로 토론해보세요")
                    .currentParticipants(2)
                    .maxParticipants(5)
                    .averageLevel(LanguageLevel.INTERMEDIATE)
                    .compatibilityScore(0.78)
                    .startTime(LocalDateTime.now().plusMinutes(15))
                    .build()
        );
    }
    
    private List<GroupSessionAlternative> findLevelBasedGroups(UUID userId, MatchingPreferences preferences) {
        return Arrays.asList(
            GroupSessionAlternative.builder()
                    .sessionId("level-inter-001")
                    .title("중급자 회화 그룹")
                    .description("중급 수준의 편안한 회화 연습")
                    .currentParticipants(4)
                    .maxParticipants(6)
                    .averageLevel(LanguageLevel.INTERMEDIATE)
                    .compatibilityScore(0.92)
                    .startTime(LocalDateTime.now().plusMinutes(5))
                    .build()
        );
    }
    
    private List<GroupSessionAlternative> findTimeBasedGroups(UUID userId, MatchingPreferences preferences) {
        return Arrays.asList(
            GroupSessionAlternative.builder()
                    .sessionId("evening-chat-001")
                    .title("저녁 프리토킹")
                    .description("편안한 분위기에서 자유롭게 대화해요")
                    .currentParticipants(2)
                    .maxParticipants(4)
                    .averageLevel(LanguageLevel.UPPER_INTERMEDIATE)
                    .compatibilityScore(0.80)
                    .startTime(LocalDateTime.now().plusMinutes(8))
                    .build()
        );
    }
    
    private AiPartnerRecommendation createBasicConversationAi(MatchingPreferences preferences) {
        return AiPartnerRecommendation.builder()
                .partnerId("ai-basic-001")
                .name("Alex (기초 회화 도우미)")
                .description("친근한 성격의 기초 회화 연습 파트너")
                .languageLevel(LanguageLevel.BEGINNER)
                .specialization(Arrays.asList("일상 대화", "기초 문법", "발음 교정"))
                .personality(AiPersonality.FRIENDLY)
                .relevanceScore(0.95)
                .build();
    }
    
    private AiPartnerRecommendation createDiscussionAi(MatchingPreferences preferences) {
        return AiPartnerRecommendation.builder()
                .partnerId("ai-discussion-001")
                .name("Taylor (토론 파트너)")
                .description("다양한 주제로 토론할 수 있는 지적인 파트너")
                .languageLevel(LanguageLevel.INTERMEDIATE)
                .specialization(Arrays.asList("시사 토론", "문화 교류", "논리적 사고"))
                .personality(AiPersonality.INTELLECTUAL)
                .relevanceScore(0.88)
                .build();
    }
    
    private AiPartnerRecommendation createBusinessAi(MatchingPreferences preferences) {
        return AiPartnerRecommendation.builder()
                .partnerId("ai-business-001")
                .name("Morgan (비즈니스 코치)")
                .description("비즈니스 영어 전문 코칭 파트너")
                .languageLevel(LanguageLevel.ADVANCED)
                .specialization(Arrays.asList("비즈니스 영어", "프레젠테이션", "협상 스킬"))
                .personality(AiPersonality.PROFESSIONAL)
                .relevanceScore(0.90)
                .build();
    }
    
    private List<AiPartnerRecommendation> createSpecializedAiPartners(UUID userId, MatchingPreferences preferences) {
        return Arrays.asList(
            AiPartnerRecommendation.builder()
                    .partnerId("ai-culture-001")
                    .name("Kim (문화 교류 전문가)")
                    .description("한국 문화를 영어로 소개하는 전문 파트너")
                    .languageLevel(preferences.getCurrentLanguageLevel())
                    .specialization(Arrays.asList("문화 교류", "여행", "음식"))
                    .personality(AiPersonality.CULTURAL)
                    .relevanceScore(0.82)
                    .build()
        );
    }
    
    // 기타 헬퍼 메서드들...
    private boolean isOptimalTime() { return true; }
    private double getUserActivityScore(UUID userId) { return 1.0; }
    private void cacheAlternativeResponse(UUID userId, MatchingAlternativeResponse response) {}
    private void updateUserPreferences(UUID userId, MatchingFailureContext context) {}
    private RelaxedCriteria analyzeRelaxableCriteria(UUID userId, MatchingPreferences preferences) { 
        // TODO: RelaxedCriteria Lombok 이슈로 임시 처리 - 추후 수정 필요
        return new RelaxedCriteria();
    }
    private double calculateAgeRangeImpact(UUID userId, MatchingPreferences preferences) { return 0.2; }
    private double calculateLanguageLevelImpact(UUID userId, MatchingPreferences preferences) { return 0.15; }
    private double calculateLocationImpact(UUID userId, MatchingPreferences preferences) { return 0.3; }
    private double calculateTimeSlotImpact(UUID userId, MatchingPreferences preferences) { return 0.25; }
    private Duration calculateWaitTimeReduction(Map<String, Double> improvements) { 
        return Duration.ofMinutes(10); 
    }
    private MatchingHistory getUserMatchingHistory(UUID userId) { 
        return MatchingHistory.builder().successRate(0.7).failureRate(0.3).build(); 
    }
    private ProfileCompleteness analyzeProfileCompleteness(UUID userId) { 
        return ProfileCompleteness.builder().score(0.85).build(); 
    }
    private TimeSlotAnalysis analyzeOptimalTimeSlots(UUID userId) { 
        return TimeSlotAnalysis.builder().isOptimal(false).optimalTimeSlot("19:00-21:00").improvement(0.3).build(); 
    }
    private double calculatePotentialSuccessRate(MatchingHistory history, List<ImprovementSuggestion> suggestions) { 
        return history.getSuccessRate() + 0.2; 
    }
    
    // === 내부 데이터 클래스들 ===
    
    public enum MatchingFailureReason {
        NO_AVAILABLE_USERS, CRITERIA_TOO_STRICT, PEAK_TIME_CONGESTION, TECHNICAL_ERROR
    }
    
    public enum AlternativeType {
        GROUP_SESSION, AI_PARTNER, RELAXED_CRITERIA, SCHEDULED_SESSION
    }
    
    public enum AlternativePriority {
        HIGH, MEDIUM, LOW
    }
    
    public enum LanguageLevel {
        BEGINNER, ELEMENTARY, INTERMEDIATE, UPPER_INTERMEDIATE, ADVANCED, PROFICIENT
    }
    
    public enum AiPersonality {
        FRIENDLY, INTELLECTUAL, PROFESSIONAL, CULTURAL, CASUAL
    }
    
    public enum ImprovementType {
        PROFILE_COMPLETION, TIME_OPTIMIZATION, LANGUAGE_LEVEL_REASSESSMENT, INTEREST_EXPANSION
    }
    
    public enum ImprovementImpact {
        HIGH, MEDIUM, LOW
    }
    
    public enum EffortLevel {
        LOW, MEDIUM, HIGH
    }
    
    // 데이터 클래스들 (lombok 사용)
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingFailureContext {
        private MatchingFailureReason failureReason;
        private LocalDateTime failureTime;
        private long waitTimeBefore;
        private MatchingPreferences originalPreferences;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class FailureAnalysis {
        private MatchingFailureReason primaryReason;
        private List<String> contributingFactors;
        private LocalDateTime analysisTime;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingPreferences {
        private LanguageLevel currentLanguageLevel;
        private List<String> interests;
        private String preferredTimeSlot;
        private String location;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingAlternative {
        private AlternativeType type;
        private String title;
        private String description;
        private Duration estimatedWaitTime;
        private double successProbability;
        private List<String> benefits;
        private AlternativePriority priority;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingSuccessPrediction {
        private double probabilityWithinHour;
        private double probabilityWithinDay;
        private LocalDateTime optimalRetryTime;
        private double confidenceLevel;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingAlternativeResponse {
        private UUID userId;
        private FailureAnalysis failureAnalysis;
        private List<MatchingAlternative> alternatives;
        private List<String> recommendations;
        private MatchingSuccessPrediction successPrediction;
        private LocalDateTime generatedAt;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class GroupSessionAlternative {
        private String sessionId;
        private String title;
        private String description;
        private int currentParticipants;
        private int maxParticipants;
        private LanguageLevel averageLevel;
        private double compatibilityScore;
        private LocalDateTime startTime;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class AiPartnerRecommendation {
        private String partnerId;
        private String name;
        private String description;
        private LanguageLevel languageLevel;
        private List<String> specialization;
        private AiPersonality personality;
        private double relevanceScore;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class RelaxedCriteria {
        private boolean canRelaxAgeRange;
        private boolean canRelaxLanguageLevel;
        private boolean canRelaxLocation;
        private boolean canRelaxTimeSlots;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class RelaxedMatchingOptions {
        private UUID userId;
        private MatchingPreferences originalPreferences;
        private RelaxedCriteria relaxedCriteria;
        private Map<String, Double> probabilityImprovements;
        private Duration estimatedWaitTimeReduction;
        private LocalDateTime generatedAt;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class DynamicAlternative {
        private AlternativeType type;
        private String title;
        private String description;
        private Duration estimatedWaitTime;
        private AlternativePriority priority;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class DynamicAlternativeResponse {
        private UUID userId;
        private Duration currentWaitTime;
        private List<DynamicAlternative> alternatives;
        private LocalDateTime nextCheckIn;
        private LocalDateTime generatedAt;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ImprovementSuggestion {
        private ImprovementType type;
        private String title;
        private String description;
        private ImprovementImpact impact;
        private EffortLevel effort;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingImprovementSuggestions {
        private UUID userId;
        private double currentSuccessRate;
        private List<ImprovementSuggestion> suggestions;
        private double potentialSuccessRate;
        private LocalDateTime generatedAt;
    }
    
    // 추가 헬퍼 클래스들
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class MatchingHistory {
        private double successRate;
        private double failureRate;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ProfileCompleteness {
        private double score;
    }
    
    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class TimeSlotAnalysis {
        private boolean isOptimal;
        private String optimalTimeSlot;
        private double improvement;
    }
}