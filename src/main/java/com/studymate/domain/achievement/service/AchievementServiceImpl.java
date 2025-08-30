package com.studymate.domain.achievement.service;

import com.studymate.domain.achievement.domain.dto.response.AchievementResponse;
import com.studymate.domain.achievement.domain.dto.response.AchievementStatsResponse;
import com.studymate.domain.achievement.domain.dto.response.UserAchievementResponse;
import com.studymate.domain.achievement.domain.repository.AchievementRepository;
import com.studymate.domain.achievement.domain.repository.UserAchievementRepository;
import com.studymate.domain.achievement.entity.Achievement;
import com.studymate.domain.achievement.entity.UserAchievement;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Override
    public List<AchievementResponse> getAllActiveAchievements() {
        return achievementRepository.findByIsActiveTrue()
            .stream()
            .map(AchievementResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<AchievementResponse> getAchievementsByCategory(Achievement.AchievementCategory category) {
        return achievementRepository.findByCategoryAndIsActiveTrueOrderBySortOrderAsc(category)
            .stream()
            .map(AchievementResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementResponse> getUserAchievements(User user) {
        return userAchievementRepository.findByUserOrderByCompletedAtDesc(user)
            .stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementResponse> getCompletedAchievements(User user) {
        return userAchievementRepository.findByUserAndIsCompletedTrueOrderByCompletedAtDesc(user)
            .stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementResponse> getInProgressAchievements(User user) {
        return userAchievementRepository.findByUserAndIsCompletedFalseOrderByCurrentProgressDesc(user)
            .stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public AchievementStatsResponse getAchievementStats(User user) {
        Long totalAchievements = (long) achievementRepository.findByIsActiveTrue().size();
        Long completedAchievements = userAchievementRepository.countCompletedAchievementsByUser(user);
        Long inProgressAchievements = userAchievementRepository.countInProgressAchievementsByUser(user);
        Long totalXpEarned = userAchievementRepository.getTotalXpByUser(user);
        Long unclaimedRewards = (long) userAchievementRepository
            .findByUserAndIsCompletedTrueAndIsRewardClaimedFalseOrderByCompletedAtAsc(user).size();
        
        Double completionRate = totalAchievements > 0 ? 
            (double) completedAchievements / totalAchievements * 100 : 0.0;

        // 카테고리별 통계
        Map<String, Long> achievementsByCategory = new HashMap<>();
        for (Achievement.AchievementCategory category : Achievement.AchievementCategory.values()) {
            Long count = userAchievementRepository.countCompletedAchievementsByUserAndCategory(user, category);
            achievementsByCategory.put(category.name(), count);
        }

        // 티어별 통계
        Map<String, Long> achievementsByTier = new HashMap<>();
        for (Achievement.AchievementTier tier : Achievement.AchievementTier.values()) {
            Long count = userAchievementRepository.countCompletedAchievementsByUserAndTier(user, tier);
            achievementsByTier.put(tier.name(), count);
        }

        // 최근 완료 성취
        List<UserAchievementResponse> recentCompletions = userAchievementRepository
            .findRecentCompletedAchievements(user, 5)
            .stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());

        // 거의 완료된 성취
        List<UserAchievementResponse> nearCompletion = userAchievementRepository
            .findNearCompletionAchievements(user)
            .stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());

        return AchievementStatsResponse.builder()
            .totalAchievements(totalAchievements)
            .completedAchievements(completedAchievements)
            .inProgressAchievements(inProgressAchievements)
            .totalXpEarned(totalXpEarned)
            .unclaimedRewards(unclaimedRewards)
            .completionRate(completionRate)
            .achievementsByCategory(achievementsByCategory)
            .achievementsByTier(achievementsByTier)
            .recentCompletions(recentCompletions)
            .nearCompletion(nearCompletion)
            .build();
    }

    @Override
    @Transactional
    public UserAchievementResponse updateProgress(User user, String achievementKey, Integer progress) {
        Achievement achievement = achievementRepository.findByAchievementKeyAndIsActiveTrue(achievementKey)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 성취입니다: " + achievementKey));

        UserAchievement userAchievement = userAchievementRepository
            .findByUserAndAchievement(user, achievement)
            .orElseGet(() -> {
                UserAchievement newUserAchievement = UserAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .build();
                return userAchievementRepository.save(newUserAchievement);
            });

        userAchievement.updateProgress(progress);
        userAchievement = userAchievementRepository.save(userAchievement);

        log.info("성취 진행도 업데이트: 사용자={}, 성취={}, 진행도={}/{}", 
            user.getId(), achievementKey, progress, achievement.getTargetValue());

        return UserAchievementResponse.from(userAchievement);
    }

    @Override
    @Transactional
    public UserAchievementResponse incrementProgress(User user, String achievementKey, Integer increment) {
        Achievement achievement = achievementRepository.findByAchievementKeyAndIsActiveTrue(achievementKey)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 성취입니다: " + achievementKey));

        UserAchievement userAchievement = userAchievementRepository
            .findByUserAndAchievement(user, achievement)
            .orElseGet(() -> {
                UserAchievement newUserAchievement = UserAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .build();
                return userAchievementRepository.save(newUserAchievement);
            });

        userAchievement.incrementProgress(increment);
        userAchievement = userAchievementRepository.save(userAchievement);

        log.info("성취 진행도 증가: 사용자={}, 성취={}, 증가량={}, 현재진행도={}", 
            user.getId(), achievementKey, increment, userAchievement.getCurrentProgress());

        return UserAchievementResponse.from(userAchievement);
    }

    @Override
    @Transactional
    public UserAchievementResponse claimReward(User user, Long userAchievementId) {
        UserAchievement userAchievement = userAchievementRepository.findById(userAchievementId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 성취입니다"));

        if (!userAchievement.getUser().equals(user)) {
            throw new IllegalArgumentException("본인의 성취가 아닙니다");
        }

        if (!userAchievement.getIsCompleted()) {
            throw new IllegalArgumentException("완료되지 않은 성취입니다");
        }

        if (userAchievement.getIsRewardClaimed()) {
            throw new IllegalArgumentException("이미 보상을 수령했습니다");
        }

        userAchievement.claimReward();
        userAchievement = userAchievementRepository.save(userAchievement);

        log.info("성취 보상 수령: 사용자={}, 성취={}, XP={}", 
            user.getId(), userAchievement.getAchievement().getAchievementKey(), 
            userAchievement.getAchievement().getXpReward());

        return UserAchievementResponse.from(userAchievement);
    }

    @Override
    @Transactional
    public void initializeUserAchievements(User user) {
        List<Achievement> achievements = achievementRepository.findByIsActiveTrue();
        List<UserAchievement> userAchievements = new ArrayList<>();

        for (Achievement achievement : achievements) {
            // 이미 존재하는 성취는 스킵
            if (userAchievementRepository.findByUserAndAchievement(user, achievement).isPresent()) {
                continue;
            }

            UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .build();
            userAchievements.add(userAchievement);
        }

        if (!userAchievements.isEmpty()) {
            userAchievementRepository.saveAll(userAchievements);
            log.info("사용자 성취 초기화 완료: 사용자={}, 성취수={}", user.getId(), userAchievements.size());
        }
    }

    @Override
    @Transactional
    public List<UserAchievementResponse> checkAndCompleteAchievements(User user) {
        List<UserAchievement> inProgressAchievements = userAchievementRepository
            .findByUserAndIsCompletedFalseOrderByCurrentProgressDesc(user);
        
        List<UserAchievement> completedAchievements = new ArrayList<>();
        
        for (UserAchievement userAchievement : inProgressAchievements) {
            Achievement achievement = userAchievement.getAchievement();
            
            // 선행 조건 확인
            if (achievement.getPrerequisiteAchievementId() != null) {
                boolean hasPrerequisite = userAchievementRepository
                    .findByUserAndAchievement(user, 
                        achievementRepository.findById(achievement.getPrerequisiteAchievementId()).orElse(null))
                    .map(UserAchievement::getIsCompleted)
                    .orElse(false);
                    
                if (!hasPrerequisite) {
                    continue;
                }
            }
            
            // 목표 달성 확인
            if (achievement.getTargetValue() != null && 
                userAchievement.getCurrentProgress() >= achievement.getTargetValue()) {
                userAchievement.updateProgress(userAchievement.getCurrentProgress());
                completedAchievements.add(userAchievement);
            }
        }
        
        if (!completedAchievements.isEmpty()) {
            userAchievementRepository.saveAll(completedAchievements);
            log.info("성취 자동 완료: 사용자={}, 완료된성취수={}", user.getId(), completedAchievements.size());
        }
        
        return completedAchievements.stream()
            .map(UserAchievementResponse::from)
            .collect(Collectors.toList());
    }
}