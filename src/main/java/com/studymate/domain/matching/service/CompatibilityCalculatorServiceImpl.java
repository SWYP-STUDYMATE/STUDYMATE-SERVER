package com.studymate.domain.matching.service;

import com.studymate.domain.matching.domain.dto.response.CompatibilityScoreResponse;
import com.studymate.domain.onboarding.domain.repository.OnboardingPersonalityRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingStudyGoalRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingTopicRepository;
import com.studymate.domain.onboarding.entity.OnboardingPersonality;
import com.studymate.domain.onboarding.entity.OnboardingStudyGoal;
import com.studymate.domain.onboarding.entity.OnboardingTopic;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompatibilityCalculatorServiceImpl implements CompatibilityCalculatorService {

    private final OnboardingPersonalityRepository personalityRepository;
    private final OnboardingStudyGoalRepository studyGoalRepository;
    private final OnboardingTopicRepository topicRepository;

    @Override
    public CompatibilityScoreResponse calculateCompatibility(User user1, User user2) {
        Map<String, Double> categoryScores = new HashMap<>();
        
        // 언어 호환성 (30%)
        double languageScore = calculateLanguageCompatibility(user1, user2);
        categoryScores.put("language", languageScore);
        
        // 성격 호환성 (25%)
        double personalityScore = calculatePersonalityCompatibility(user1, user2);
        categoryScores.put("personality", personalityScore);
        
        // 학습 목표 호환성 (25%)
        double goalScore = calculateGoalCompatibility(user1, user2);
        categoryScores.put("goals", goalScore);
        
        // 관심사 호환성 (20%)
        double interestScore = calculateInterestCompatibility(user1, user2);
        categoryScores.put("interests", interestScore);
        
        // 전체 점수 계산 (가중 평균)
        double overallScore = (languageScore * 0.3 + personalityScore * 0.25 + 
                              goalScore * 0.25 + interestScore * 0.2);
        
        String compatibilityLevel = determineCompatibilityLevel(overallScore);
        String recommendation = generateRecommendation(overallScore, categoryScores);
        
        return new CompatibilityScoreResponse(overallScore, categoryScores, 
                                            compatibilityLevel, recommendation);
    }

    @Override
    public double calculateSimpleScore(User user1, User user2) {
        return calculateCompatibility(user1, user2).getOverallScore();
    }

    private double calculateLanguageCompatibility(User user1, User user2) {
        double score = 0.0;
        
        // 모국어 - 목표언어 매칭 (핵심 호환성)
        if (user1.getNativeLanguage() != null && user2.getNativeLanguage() != null) {
            // 서로의 모국어가 상대방이 배우고 싶어하는 언어인 경우 높은 점수
            boolean perfectMatch = isLanguagePairMatch(user1, user2);
            if (perfectMatch) {
                score += 80.0; // 완벽한 언어 교환 쌍
            } else {
                // 부분적 매칭 확인
                score += calculatePartialLanguageMatch(user1, user2);
            }
        }
        
        // 언어 레벨 호환성 (너무 차이나지 않는 것이 좋음)
        score += calculateLanguageLevelCompatibility(user1, user2);
        
        return Math.min(100.0, score);
    }

    private boolean isLanguagePairMatch(User user1, User user2) {
        // 실제 구현에서는 온보딩 데이터에서 목표 언어를 확인해야 함
        // 현재는 기본 로직만 구현
        return !user1.getNativeLanguage().equals(user2.getNativeLanguage());
    }

    private double calculatePartialLanguageMatch(User user1, User user2) {
        // 같은 언어권이거나 비슷한 언어 패밀리인 경우 부분 점수
        return 40.0;
    }

    private double calculateLanguageLevelCompatibility(User user1, User user2) {
        // 온보딩 데이터에서 언어 레벨을 가져와서 비교
        // 현재는 기본값 반환
        return 20.0;
    }

    private double calculatePersonalityCompatibility(User user1, User user2) {
        List<OnboardingPersonality> user1Personalities = personalityRepository.findByUserId(user1.getUserId());
        List<OnboardingPersonality> user2Personalities = personalityRepository.findByUserId(user2.getUserId());
        
        if (user1Personalities.isEmpty() || user2Personalities.isEmpty()) {
            return 50.0; // 기본 점수
        }
        
        // 성격 특성 매칭 로직
        Set<String> user1Traits = new HashSet<>();
        Set<String> user2Traits = new HashSet<>();
        
        user1Personalities.forEach(p -> user1Traits.add(p.getPersonalityType()));
        user2Personalities.forEach(p -> user2Traits.add(p.getPersonalityType()));
        
        // 공통 성격 특성 개수
        Set<String> commonTraits = new HashSet<>(user1Traits);
        commonTraits.retainAll(user2Traits);
        
        // 보완적인 성격 특성도 고려 (내향 vs 외향 등)
        double complementaryScore = calculateComplementaryPersonalityScore(user1Traits, user2Traits);
        
        double commonScore = (commonTraits.size() * 20.0) + complementaryScore;
        return Math.min(100.0, commonScore);
    }

    private double calculateComplementaryPersonalityScore(Set<String> user1Traits, Set<String> user2Traits) {
        // 보완적인 성격 조합에 대한 점수 계산
        double score = 0.0;
        
        // 예시: 내향적인 사람과 외향적인 사람의 조합
        if ((user1Traits.contains("INTROVERT") && user2Traits.contains("EXTROVERT")) ||
            (user1Traits.contains("EXTROVERT") && user2Traits.contains("INTROVERT"))) {
            score += 30.0;
        }
        
        return score;
    }

    private double calculateGoalCompatibility(User user1, User user2) {
        List<OnboardingStudyGoal> user1Goals = studyGoalRepository.findByUserId(user1.getUserId());
        List<OnboardingStudyGoal> user2Goals = studyGoalRepository.findByUserId(user2.getUserId());
        
        if (user1Goals.isEmpty() || user2Goals.isEmpty()) {
            return 50.0; // 기본 점수
        }
        
        Set<String> user1GoalTypes = new HashSet<>();
        Set<String> user2GoalTypes = new HashSet<>();
        
        user1Goals.forEach(g -> user1GoalTypes.add(g.getGoalType()));
        user2Goals.forEach(g -> user2GoalTypes.add(g.getGoalType()));
        
        // 공통 목표 개수
        Set<String> commonGoals = new HashSet<>(user1GoalTypes);
        commonGoals.retainAll(user2GoalTypes);
        
        // 공통 목표가 많을수록 높은 점수
        double score = commonGoals.size() * 25.0;
        
        // 보완적인 목표도 고려 (예: 비즈니스 + 일상회화)
        if (isComplementaryGoals(user1GoalTypes, user2GoalTypes)) {
            score += 20.0;
        }
        
        return Math.min(100.0, score);
    }

    private boolean isComplementaryGoals(Set<String> user1Goals, Set<String> user2Goals) {
        // 보완적인 목표 조합 확인 로직
        return (user1Goals.contains("BUSINESS") && user2Goals.contains("CASUAL")) ||
               (user1Goals.contains("ACADEMIC") && user2Goals.contains("PRACTICAL"));
    }

    private double calculateInterestCompatibility(User user1, User user2) {
        List<OnboardingTopic> user1Topics = topicRepository.findByUsrId(user1.getUserId());
        List<OnboardingTopic> user2Topics = topicRepository.findByUsrId(user2.getUserId());
        
        if (user1Topics.isEmpty() || user2Topics.isEmpty()) {
            return 50.0; // 기본 점수
        }
        
        Set<String> user1Interests = new HashSet<>();
        Set<String> user2Interests = new HashSet<>();
        
        user1Topics.forEach(t -> user1Interests.add(t.getTopicName()));
        user2Topics.forEach(t -> user2Interests.add(t.getTopicName()));
        
        // 공통 관심사 개수
        Set<String> commonInterests = new HashSet<>(user1Interests);
        commonInterests.retainAll(user2Interests);
        
        // 공통 관심사 비율
        int totalInterests = user1Interests.size() + user2Interests.size();
        double commonRatio = totalInterests > 0 ? (double) commonInterests.size() * 2 / totalInterests : 0;
        
        return commonRatio * 100.0;
    }

    private String determineCompatibilityLevel(double overallScore) {
        if (overallScore >= 80.0) {
            return "HIGH";
        } else if (overallScore >= 60.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private String generateRecommendation(double overallScore, Map<String, Double> categoryScores) {
        StringBuilder recommendation = new StringBuilder();
        
        if (overallScore >= 80.0) {
            recommendation.append("매우 좋은 매칭입니다! ");
        } else if (overallScore >= 60.0) {
            recommendation.append("괜찮은 매칭입니다. ");
        } else {
            recommendation.append("호환성이 낮을 수 있습니다. ");
        }
        
        // 가장 높은 점수의 카테고리 언급
        String bestCategory = categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
        
        switch (bestCategory) {
            case "language":
                recommendation.append("언어 교환에 최적화된 파트너입니다.");
                break;
            case "personality":
                recommendation.append("성격이 잘 맞는 파트너입니다.");
                break;
            case "goals":
                recommendation.append("학습 목표가 비슷한 파트너입니다.");
                break;
            case "interests":
                recommendation.append("공통 관심사가 많은 파트너입니다.");
                break;
            default:
                recommendation.append("함께 학습해보세요!");
        }
        
        return recommendation.toString();
    }
}