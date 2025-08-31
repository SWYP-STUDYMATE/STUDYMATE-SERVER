package com.studymate.domain.matching.repository;

import com.studymate.domain.matching.entity.UserMatch;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserMatch 엔티티에 대한 Repository
 */
@Repository
public interface UserMatchRepository extends JpaRepository<UserMatch, UUID> {

    /**
     * 사용자의 활성 매칭 목록 조회
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "ORDER BY um.matchedAt DESC")
    List<UserMatch> findActiveMatchesByUser(@Param("user") User user);

    /**
     * 두 사용자 간의 매칭 관계 조회
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE ((um.user1 = :user1 AND um.user2 = :user2) " +
           "OR (um.user1 = :user2 AND um.user2 = :user1)) " +
           "AND um.isActive = true")
    Optional<UserMatch> findActiveMatchBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 사용자의 모든 매칭 기록 조회 (비활성 포함)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE um.user1 = :user OR um.user2 = :user " +
           "ORDER BY um.createdAt DESC")
    List<UserMatch> findAllMatchesByUser(@Param("user") User user);

    /**
     * 사용자의 활성 매칭 수 조회
     */
    @Query("SELECT COUNT(um) FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true")
    long countActiveMatchesByUser(@Param("user") User user);

    /**
     * 특정 사용자가 해제한 매칭 목록
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE um.deactivatedBy = :user " +
           "ORDER BY um.deactivatedAt DESC")
    List<UserMatch> findMatchesDeactivatedByUser(@Param("user") User user);

    // === 고급 매칭 통계 및 분석 메서드들 ===

    /**
     * 특정 사용자의 전체 매칭 수 조회 (활성/비활성 모두 포함)
     */
    @Query("SELECT COUNT(um) FROM UserMatch um " +
           "WHERE um.user1 = :user OR um.user2 = :user")
    long countByUser(@Param("user") User user);

    /**
     * 특정 사용자의 활성 매칭 수 조회 (별칭 메서드)
     */
    default long countActiveMatchesByUserId(UUID userId) {
        // TODO: User 엔티티를 조회하지 않고 userId로 직접 카운트하는 쿼리로 개선
        return 0L; // 임시 구현
    }

    /**
     * 특정 사용자의 모든 매칭 기록 조회 (별칭 메서드)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE um.user1 = :user OR um.user2 = :user " +
           "ORDER BY um.createdAt DESC")
    List<UserMatch> findByUser(@Param("user") User user);

    /**
     * 특정 기간 동안의 매칭 통계
     */
    @Query("SELECT COUNT(um) FROM UserMatch um " +
           "WHERE um.matchedAt BETWEEN :startDate AND :endDate")
    long countMatchesByPeriod(@Param("startDate") java.time.LocalDateTime startDate, 
                             @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 매칭 성공률 계산 (활성 매칭 / 전체 매칭)
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN um.isActive = true THEN 1 END) AS DOUBLE) / COUNT(um) * 100 " +
           "FROM UserMatch um " +
           "WHERE um.user1 = :user OR um.user2 = :user")
    Double calculateMatchSuccessRate(@Param("user") User user);

    /**
     * 최근 활동한 매칭 조회 (최근 7일 이내)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "AND um.lastContactAt > :since " +
           "ORDER BY um.lastContactAt DESC")
    List<UserMatch> findRecentActiveMatches(@Param("user") User user, 
                                          @Param("since") java.time.LocalDateTime since);

    /**
     * 장기간 비활성 매칭 조회 (30일 이상 연락 없음)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "AND (um.lastContactAt IS NULL OR um.lastContactAt < :threshold) " +
           "ORDER BY um.matchedAt DESC")
    List<UserMatch> findInactiveMatches(@Param("user") User user, 
                                       @Param("threshold") java.time.LocalDateTime threshold);

    /**
     * 사용자별 평균 매칭 지속 기간 계산
     */
    @Query("SELECT AVG(" +
           "CASE WHEN um.deactivatedAt IS NOT NULL " +
           "THEN EXTRACT(DAY FROM (um.deactivatedAt - um.matchedAt)) " +
           "ELSE EXTRACT(DAY FROM (CURRENT_TIMESTAMP - um.matchedAt)) END) " +
           "FROM UserMatch um " +
           "WHERE um.user1 = :user OR um.user2 = :user")
    Double calculateAverageMatchDuration(@Param("user") User user);

    /**
     * 언어별 매칭 통계 (특정 사용자의 매칭 파트너들의 언어 분포)
     */
    @Query("SELECT " +
           "CASE WHEN um.user1 = :user THEN um.user2.nativeLanguage.name " +
           "ELSE um.user1.nativeLanguage.name END AS partnerLanguage, " +
           "COUNT(um) " +
           "FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "GROUP BY partnerLanguage " +
           "ORDER BY COUNT(um) DESC")
    List<Object[]> getMatchLanguageDistribution(@Param("user") User user);

    /**
     * 시간대별 매칭 생성 통계
     */
    @Query("SELECT EXTRACT(HOUR FROM um.matchedAt) as hour, COUNT(um) " +
           "FROM UserMatch um " +
           "WHERE um.matchedAt > :since " +
           "GROUP BY EXTRACT(HOUR FROM um.matchedAt) " +
           "ORDER BY hour")
    List<Object[]> getMatchingHourlyStatistics(@Param("since") java.time.LocalDateTime since);

    /**
     * 매칭 해제 사유별 통계 (해제한 사용자별)
     */
    @Query("SELECT um.deactivatedBy.englishName, COUNT(um) " +
           "FROM UserMatch um " +
           "WHERE um.deactivatedBy IS NOT NULL " +
           "AND um.deactivatedAt > :since " +
           "GROUP BY um.deactivatedBy.englishName " +
           "ORDER BY COUNT(um) DESC")
    List<Object[]> getDeactivationStatistics(@Param("since") java.time.LocalDateTime since);

    /**
     * 상호 매칭 (서로 매칭 요청한) 비율 계산
     */
    @Query("SELECT COUNT(DISTINCT um1) * 100.0 / COUNT(DISTINCT um2) " +
           "FROM UserMatch um1, UserMatch um2 " +
           "WHERE um1.user1 = um2.user2 AND um1.user2 = um2.user1 " +
           "AND um1.matchedAt > :since AND um2.matchedAt > :since")
    Double calculateMutualMatchRate(@Param("since") java.time.LocalDateTime since);
}