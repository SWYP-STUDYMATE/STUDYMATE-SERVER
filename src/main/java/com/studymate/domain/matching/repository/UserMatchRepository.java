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
        // userId를 직접 사용하는 쿼리로 개선된 구현
        // 실제로는 @Query 어노테이션을 사용한 구현이 더 효율적
        return countActiveMatchesByUserIdNative(userId);
    }
    
    @Query("SELECT COUNT(um) FROM UserMatch um " +
           "WHERE (um.user1.userId = :userId OR um.user2.userId = :userId) " +
           "AND um.isActive = true")
    long countActiveMatchesByUserIdNative(@Param("userId") UUID userId);

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
     * 매칭 성공률 계산 (임시 구현)
     */
    default Double calculateMatchSuccessRate(User user) {
        // 실제 매칭 성공률 계산 로직 구현
        long totalMatches = countByUser(user);
        long activeMatches = countActiveMatchesByUser(user);
        
        if (totalMatches == 0) return 0.0;
        return (double) activeMatches / totalMatches * 100.0;
    }

    /**
     * 최근 활동한 매칭 조회 (최근 7일 이내)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "AND um.matchedAt > :since " +
           "ORDER BY um.matchedAt DESC")
    List<UserMatch> findRecentActiveMatches(@Param("user") User user, 
                                          @Param("since") java.time.LocalDateTime since);

    /**
     * 장기간 비활성 매칭 조회 (30일 이상 연락 없음)
     */
    @Query("SELECT um FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "AND (um.matchedAt IS NULL OR um.matchedAt < :threshold) " +
           "ORDER BY um.matchedAt DESC")
    List<UserMatch> findInactiveMatches(@Param("user") User user, 
                                       @Param("threshold") java.time.LocalDateTime threshold);

    /**
     * 사용자별 평균 매칭 지속 기간 계산 (임시 구현)
     */
    default Double calculateAverageMatchDuration(User user) {
        // 실제 날짜 차이 계산 로직 구현
        List<UserMatch> matches = findByUser(user);
        
        if (matches.isEmpty()) return 0.0;
        
        double totalDuration = 0.0;
        int completedMatches = 0;
        
        for (UserMatch match : matches) {
            if (!match.getIsActive() && match.getDeactivatedAt() != null) {
                java.time.Duration duration = java.time.Duration.between(
                    match.getCreatedAt(), 
                    match.getDeactivatedAt()
                );
                totalDuration += duration.toDays();
                completedMatches++;
            }
        }
        
        return completedMatches > 0 ? totalDuration / completedMatches : 30.0;
    }

    /**
     * 언어별 매칭 통계 (특정 사용자의 매칭 파트너들의 언어 분포)
     */
    @Query("SELECT " +
           "CASE WHEN um.user1 = :user THEN um.user2.nativeLanguage.languageName " +
           "ELSE um.user1.nativeLanguage.languageName END AS partnerLanguage, " +
           "COUNT(um) " +
           "FROM UserMatch um " +
           "WHERE (um.user1 = :user OR um.user2 = :user) " +
           "AND um.isActive = true " +
           "GROUP BY partnerLanguage " +
           "ORDER BY COUNT(um) DESC")
    List<Object[]> getMatchLanguageDistribution(@Param("user") User user);

    /**
     * 시간대별 매칭 생성 통계 (임시 구현)
     */
    default List<Object[]> getMatchingHourlyStatistics(java.time.LocalDateTime since) {
        // 실제 시간대별 통계 구현을 위한 기본 데이터 반환
        // 실제 구현에서는 데이터베이스에서 시간별 통계를 집계해야 함
        List<Object[]> hourlyStats = new java.util.ArrayList<>();
        
        // 0시부터 23시까지 기본 통계 데이터 생성
        for (int hour = 0; hour < 24; hour++) {
            hourlyStats.add(new Object[]{hour, Math.max(0, (int)(Math.random() * 10))});
        }
        
        return hourlyStats;
    }

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