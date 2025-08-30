package com.studymate.domain.achievement.config;

import com.studymate.domain.achievement.domain.repository.AchievementRepository;
import com.studymate.domain.achievement.entity.Achievement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AchievementDataLoader implements CommandLineRunner {

    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        if (achievementRepository.count() == 0) {
            loadInitialAchievements();
            log.info("성취 초기 데이터 로드 완료");
        }
    }

    private void loadInitialAchievements() {
        List<Achievement> achievements = Arrays.asList(
            // 학습 관련 성취
            Achievement.builder()
                .achievementKey("first_session")
                .title("첫 세션 완료")
                .description("첫 번째 화상 세션을 완료했습니다!")
                .category(Achievement.AchievementCategory.LEARNING)
                .type(Achievement.AchievementType.COUNT)
                .tier(Achievement.AchievementTier.BRONZE)
                .targetValue(1)
                .targetUnit("세션")
                .xpReward(100)
                .badgeColor("#CD7F32")
                .sortOrder(1)
                .build(),
            
            Achievement.builder()
                .achievementKey("session_10")
                .title("세션 마스터")
                .description("10번의 화상 세션을 완료했습니다!")
                .category(Achievement.AchievementCategory.LEARNING)
                .type(Achievement.AchievementType.COUNT)
                .tier(Achievement.AchievementTier.SILVER)
                .targetValue(10)
                .targetUnit("세션")
                .xpReward(500)
                .badgeColor("#C0C0C0")
                .sortOrder(2)
                .build(),
            
            Achievement.builder()
                .achievementKey("session_50")
                .title("세션 전문가")
                .description("50번의 화상 세션을 완료했습니다!")
                .category(Achievement.AchievementCategory.LEARNING)
                .type(Achievement.AchievementType.COUNT)
                .tier(Achievement.AchievementTier.GOLD)
                .targetValue(50)
                .targetUnit("세션")
                .xpReward(2000)
                .badgeColor("#FFD700")
                .sortOrder(3)
                .build(),
            
            // 연속 학습 성취
            Achievement.builder()
                .achievementKey("streak_7")
                .title("일주일 연속")
                .description("7일 연속으로 세션에 참여했습니다!")
                .category(Achievement.AchievementCategory.ENGAGEMENT)
                .type(Achievement.AchievementType.STREAK)
                .tier(Achievement.AchievementTier.SILVER)
                .targetValue(7)
                .targetUnit("일")
                .xpReward(750)
                .badgeColor("#C0C0C0")
                .sortOrder(4)
                .build(),
            
            Achievement.builder()
                .achievementKey("streak_30")
                .title("한 달 연속")
                .description("30일 연속으로 세션에 참여했습니다!")
                .category(Achievement.AchievementCategory.ENGAGEMENT)
                .type(Achievement.AchievementType.STREAK)
                .tier(Achievement.AchievementTier.GOLD)
                .targetValue(30)
                .targetUnit("일")
                .xpReward(3000)
                .badgeColor("#FFD700")
                .sortOrder(5)
                .build(),
            
            // 소셜 성취
            Achievement.builder()
                .achievementKey("first_friend")
                .title("첫 친구")
                .description("첫 번째 학습 친구를 만들었습니다!")
                .category(Achievement.AchievementCategory.SOCIAL)
                .type(Achievement.AchievementType.COUNT)
                .tier(Achievement.AchievementTier.BRONZE)
                .targetValue(1)
                .targetUnit("친구")
                .xpReward(200)
                .badgeColor("#CD7F32")
                .sortOrder(6)
                .build(),
            
            Achievement.builder()
                .achievementKey("friends_5")
                .title("인기쟁이")
                .description("5명의 학습 친구를 만들었습니다!")
                .category(Achievement.AchievementCategory.SOCIAL)
                .type(Achievement.AchievementType.COUNT)
                .tier(Achievement.AchievementTier.SILVER)
                .targetValue(5)
                .targetUnit("친구")
                .xpReward(1000)
                .badgeColor("#C0C0C0")
                .sortOrder(7)
                .build(),
            
            // 시간 기반 성취
            Achievement.builder()
                .achievementKey("study_hours_10")
                .title("10시간 달성")
                .description("총 10시간의 학습을 완료했습니다!")
                .category(Achievement.AchievementCategory.TIME)
                .type(Achievement.AchievementType.ACCUMULATE)
                .tier(Achievement.AchievementTier.BRONZE)
                .targetValue(600) // 10시간 (분 단위)
                .targetUnit("분")
                .xpReward(500)
                .badgeColor("#CD7F32")
                .sortOrder(8)
                .build(),
            
            Achievement.builder()
                .achievementKey("study_hours_50")
                .title("50시간 달성")
                .description("총 50시간의 학습을 완료했습니다!")
                .category(Achievement.AchievementCategory.TIME)
                .type(Achievement.AchievementType.ACCUMULATE)
                .tier(Achievement.AchievementTier.SILVER)
                .targetValue(3000) // 50시간 (분 단위)
                .targetUnit("분")
                .xpReward(2500)
                .badgeColor("#C0C0C0")
                .sortOrder(9)
                .build(),
            
            Achievement.builder()
                .achievementKey("study_hours_100")
                .title("100시간 달성")
                .description("총 100시간의 학습을 완료했습니다!")
                .category(Achievement.AchievementCategory.TIME)
                .type(Achievement.AchievementType.ACCUMULATE)
                .tier(Achievement.AchievementTier.GOLD)
                .targetValue(6000) // 100시간 (분 단위)
                .targetUnit("분")
                .xpReward(5000)
                .badgeColor("#FFD700")
                .sortOrder(10)
                .build(),
            
            // 스킬 발전 성취
            Achievement.builder()
                .achievementKey("level_up_first")
                .title("첫 레벨업")
                .description("처음으로 레벨이 올랐습니다!")
                .category(Achievement.AchievementCategory.SKILL)
                .type(Achievement.AchievementType.THRESHOLD)
                .tier(Achievement.AchievementTier.BRONZE)
                .targetValue(2)
                .targetUnit("레벨")
                .xpReward(300)
                .badgeColor("#CD7F32")
                .sortOrder(11)
                .build(),
            
            Achievement.builder()
                .achievementKey("level_5")
                .title("중급자")
                .description("레벨 5에 도달했습니다!")
                .category(Achievement.AchievementCategory.SKILL)
                .type(Achievement.AchievementType.THRESHOLD)
                .tier(Achievement.AchievementTier.SILVER)
                .targetValue(5)
                .targetUnit("레벨")
                .xpReward(1500)
                .badgeColor("#C0C0C0")
                .sortOrder(12)
                .build(),
            
            Achievement.builder()
                .achievementKey("level_10")
                .title("고급자")
                .description("레벨 10에 도달했습니다!")
                .category(Achievement.AchievementCategory.SKILL)
                .type(Achievement.AchievementType.THRESHOLD)
                .tier(Achievement.AchievementTier.GOLD)
                .targetValue(10)
                .targetUnit("레벨")
                .xpReward(5000)
                .badgeColor("#FFD700")
                .sortOrder(13)
                .build(),
            
            // 특별 성취
            Achievement.builder()
                .achievementKey("early_adopter")
                .title("얼리 어답터")
                .description("서비스 오픈 첫 달에 가입했습니다!")
                .category(Achievement.AchievementCategory.SPECIAL)
                .type(Achievement.AchievementType.MILESTONE)
                .tier(Achievement.AchievementTier.LEGENDARY)
                .xpReward(1000)
                .badgeColor("#9932CC")
                .isHidden(true)
                .sortOrder(14)
                .build(),
            
            Achievement.builder()
                .achievementKey("perfect_week")
                .title("완벽한 한 주")
                .description("일주일 동안 매일 세션에 참여했습니다!")
                .category(Achievement.AchievementCategory.ENGAGEMENT)
                .type(Achievement.AchievementType.COMBINATION)
                .tier(Achievement.AchievementTier.PLATINUM)
                .targetValue(7)
                .targetUnit("일")
                .xpReward(2000)
                .badgeColor("#E5E4E2")
                .sortOrder(15)
                .build()
        );
        
        achievementRepository.saveAll(achievements);
    }
}