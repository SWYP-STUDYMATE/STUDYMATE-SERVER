package com.studymate.domain.matching.domain.repository;

import com.studymate.domain.matching.entity.MatchingFeedback;
import com.studymate.domain.matching.entity.UserMatch;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingFeedbackRepository extends JpaRepository<MatchingFeedback, Long> {

    /**
     * 특정 리뷰어가 작성한 피드백 조회
     */
    List<MatchingFeedback> findByReviewer(User reviewer);

    /**
     * 특정 파트너가 받은 피드백 조회
     */
    List<MatchingFeedback> findByPartner(User partner);

    /**
     * 특정 매칭에 대한 피드백 조회
     */
    List<MatchingFeedback> findByUserMatch(UserMatch userMatch);

    /**
     * 특정 리뷰어와 파트너 간의 피드백 조회
     */
    Optional<MatchingFeedback> findByReviewerAndPartner(User reviewer, User partner);

    /**
     * 특정 파트너의 평균 평점 계산 (임시 구현)
     */
    default Double calculateAverageFeedbackScore(User partner) {
        // TODO: 실제 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    /**
     * 특정 파트너가 받은 피드백을 최신순으로 페이징 조회
     */
    Page<MatchingFeedback> findByPartnerOrderByCreatedAtDesc(User partner, Pageable pageable);

    /**
     * 특정 점수 이상의 피드백 조회
     */
    @Query("SELECT mf FROM MatchingFeedback mf WHERE mf.overallRating >= :minRating " +
           "ORDER BY mf.createdAt DESC")
    List<MatchingFeedback> findByOverallRatingGreaterThanEqual(@Param("minRating") Integer minRating);

    /**
     * 특정 기간의 피드백 조회
     */
    @Query("SELECT mf FROM MatchingFeedback mf WHERE mf.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY mf.createdAt DESC")
    List<MatchingFeedback> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 긍정적 피드백 수 조회 (4점 이상)
     */
    @Query("SELECT COUNT(mf) FROM MatchingFeedback mf WHERE mf.partner = :partner AND mf.overallRating >= 4")
    long countPositiveFeedback(@Param("partner") User partner);

    /**
     * 부정적 피드백 수 조회 (2점 이하)
     */
    @Query("SELECT COUNT(mf) FROM MatchingFeedback mf WHERE mf.partner = :partner AND mf.overallRating <= 2")
    long countNegativeFeedback(@Param("partner") User partner);

    /**
     * 상세 평점별 평균 점수 계산
     */
    default Double calculateAverageCommunicationRating(User partner) {
        // TODO: 실제 커뮤니케이션 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    default Double calculateAverageLanguageLevelRating(User partner) {
        // TODO: 실제 언어 수준 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    default Double calculateAverageTeachingAbilityRating(User partner) {
        // TODO: 실제 교습 능력 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    default Double calculateAveragePatienceRating(User partner) {
        // TODO: 실제 인내심 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    default Double calculateAveragePunctualityRating(User partner) {
        // TODO: 실제 시간 엄수 평점 계산 로직 구현 필요
        return 4.0; // 임시로 4.0 반환
    }

    /**
     * 다시 매칭하고 싶다고 답한 비율 계산 (임시 구현)
     */
    default Double calculateWouldMatchAgainPercentage(User partner) {
        // TODO: 실제 재매칭 희망 비율 계산 로직 구현 필요
        return 80.0; // 임시로 80% 반환
    }

    /**
     * 문제가 보고된 피드백 조회
     */
    @Query("SELECT mf FROM MatchingFeedback mf WHERE mf.reportedIssues IS NOT NULL " +
           "AND mf.reportedIssues != '' ORDER BY mf.createdAt DESC")
    List<MatchingFeedback> findFeedbackWithIssues();

    /**
     * 특정 세션 품질 점수 범위의 피드백 조회
     */
    @Query("SELECT mf FROM MatchingFeedback mf WHERE mf.sessionQualityScore BETWEEN :minScore AND :maxScore " +
           "ORDER BY mf.sessionQualityScore DESC")
    List<MatchingFeedback> findBySessionQualityScoreBetween(@Param("minScore") Integer minScore, 
                                                           @Param("maxScore") Integer maxScore);

    /**
     * 파트너별 피드백 통계 조회 (임시 구현)
     */
    default List<Object[]> getPartnerFeedbackStatistics(LocalDateTime since, long minFeedbackCount) {
        // TODO: 실제 파트너별 통계 조회 로직 구현 필요
        return java.util.Collections.emptyList();
    }
}