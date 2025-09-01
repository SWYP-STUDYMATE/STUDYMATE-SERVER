package com.studymate.domain.analytics.domain.repository;

import com.studymate.domain.analytics.entity.LearningProgress;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    // 특정 날짜의 사용자 학습 진도 조회
    Optional<LearningProgress> findByUserAndDateAndLanguageCode(User user, LocalDate date, String languageCode);

    // 사용자의 학습 진도 기간별 조회
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.date BETWEEN :startDate AND :endDate ORDER BY lp.date DESC")
    List<LearningProgress> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // 언어별 학습 진도 조회
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.languageCode = :languageCode ORDER BY lp.date DESC")
    List<LearningProgress> findByUserIdAndLanguageCode(@Param("userId") UUID userId,
                                                      @Param("languageCode") String languageCode);

    // 사용자의 최근 학습 진도 조회
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.userId = :userId ORDER BY lp.date DESC LIMIT 30")
    List<LearningProgress> findRecentProgressByUserId(@Param("userId") UUID userId);

    // 총 XP 계산
    @Query("SELECT COALESCE(SUM(lp.xpEarned), 0) FROM LearningProgress lp WHERE lp.user.userId = :userId")
    Integer getTotalXPByUserId(@Param("userId") UUID userId);

    // 언어별 총 XP 계산
    @Query("SELECT COALESCE(SUM(lp.xpEarned), 0) FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.languageCode = :languageCode")
    Integer getTotalXPByUserIdAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    // 현재 연속 학습 일수 계산
    @Query("SELECT MAX(lp.streakDays) FROM LearningProgress lp WHERE lp.user.userId = :userId")
    Integer getCurrentStreakByUserId(@Param("userId") UUID userId);

    // 총 학습 시간 계산 (분)
    @Query("SELECT COALESCE(SUM(lp.totalSessionMinutes), 0) FROM LearningProgress lp WHERE lp.user.userId = :userId")
    Integer getTotalStudyTimeByUserId(@Param("userId") UUID userId);

    // 언어별 총 학습 시간
    @Query("SELECT COALESCE(SUM(lp.totalSessionMinutes), 0) FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.languageCode = :languageCode")
    Integer getTotalStudyTimeByUserIdAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    // 스킬별 학습 시간
    @Query("SELECT COALESCE(SUM(lp.speakingMinutes), 0), COALESCE(SUM(lp.listeningMinutes), 0), " +
           "COALESCE(SUM(lp.readingMinutes), 0), COALESCE(SUM(lp.writingMinutes), 0) " +
           "FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.languageCode = :languageCode")
    Object[] getSkillTimesByUserIdAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    // 평균 테스트 점수 (임시 구현)
    default Double getAverageTestScoreByUserId(UUID userId) {
        // TODO: 실제 평균 테스트 점수 계산 로직 구현 필요
        return 75.0; // 임시로 75점 반환
    }

    // 일별 학습 활동이 있는 사용자 수
    @Query("SELECT COUNT(DISTINCT lp.user.userId) FROM LearningProgress lp WHERE lp.date = :date AND lp.totalSessionMinutes > 0")
    Long countActiveLearnersOnDate(@Param("date") LocalDate date);

    // 주간 학습 통계
    @Query("SELECT COALESCE(SUM(lp.sessionsCompleted), 0), COALESCE(SUM(lp.totalSessionMinutes), 0), " +
           "COALESCE(SUM(lp.messagesSent), 0), COALESCE(SUM(lp.wordsLearned), 0) " +
           "FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.date BETWEEN :startDate AND :endDate")
    Object[] getWeeklyStats(@Param("userId") UUID userId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

    // 월간 학습 통계
    @Query("SELECT COALESCE(SUM(lp.sessionsCompleted), 0), COALESCE(SUM(lp.totalSessionMinutes), 0), " +
           "COALESCE(SUM(lp.xpEarned), 0), COALESCE(SUM(lp.badgesEarned), 0) " +
           "FROM LearningProgress lp WHERE lp.user.userId = :userId AND lp.date BETWEEN :startDate AND :endDate")
    Object[] getMonthlyStats(@Param("userId") UUID userId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    // 언어별 학습자 수
    @Query("SELECT lp.languageCode, COUNT(DISTINCT lp.user.userId) FROM LearningProgress lp " +
           "WHERE lp.date BETWEEN :startDate AND :endDate AND lp.totalSessionMinutes > 0 " +
           "GROUP BY lp.languageCode")
    List<Object[]> getLearnerCountByLanguage(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // 상위 학습자 (XP 기준)
    @Query("SELECT lp.user.userId, SUM(lp.xpEarned) as totalXP FROM LearningProgress lp " +
           "WHERE lp.date BETWEEN :startDate AND :endDate " +
           "GROUP BY lp.user.userId ORDER BY totalXP DESC LIMIT :limit")
    List<Object[]> getTopLearnersByXP(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("limit") Integer limit);

    // 연속 학습일 랭킹
    @Query("SELECT lp.user.userId, MAX(lp.streakDays) as maxStreak FROM LearningProgress lp " +
           "GROUP BY lp.user.userId ORDER BY maxStreak DESC LIMIT :limit")
    List<Object[]> getTopLearnersByStreak(@Param("limit") Integer limit);
}