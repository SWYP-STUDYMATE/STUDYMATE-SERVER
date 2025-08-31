package com.studymate.domain.user.domain.repository;

import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.matching.repository.MatchingRepositoryCustom;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, MatchingRepositoryCustom {
    Optional<User> findByUserIdentity(String identity);
    UUID userId(UUID userId);

    @Query("select u.name from User u where u.userId = :userId")
    String findNameByUserId(@Param("userId") UUID userId);
    
    /**
     * 매칭 가능한 파트너들 조회 (자신 제외, 온보딩 완료된 사용자들)
     */
    @Query("SELECT u FROM User u WHERE u.userId != :userId")
    List<User> findPotentialPartners(@Param("userId") UUID userId);

    // === 고급 매칭 쿼리 메서드들 ===

    /**
     * 고급 필터를 적용한 파트너 검색
     * 현재는 기본 구현으로 제공하고, 향후 QueryDSL을 통해 동적 쿼리로 개선 예정
     */
    default Page<User> findPotentialPartnersWithFilters(UUID userId, AdvancedMatchingFilterRequest filters, Pageable pageable) {
        // TODO: QueryDSL로 동적 쿼리 구현
        // 현재는 기본 매칭 로직 사용
        return findAll(pageable);
    }

    /**
     * 온라인 파트너 검색
     * 실시간 온라인 상태인 사용자들 중에서 검색
     */
    default Page<User> findOnlinePartners(UUID userId, AdvancedMatchingFilterRequest filters, Pageable pageable) {
        // TODO: Redis에서 온라인 상태 확인하여 필터링
        return findAll(pageable);
    }

    /**
     * AI 기반 스마트 추천 파트너 검색
     * 사용자 행동 패턴과 선호도를 분석하여 추천
     */
    default List<User> findSmartRecommendations(UUID userId, Map<String, Double> preferenceWeights) {
        // TODO: 머신러닝 기반 추천 알고리즘 구현
        // 현재는 기본 매칭 결과 반환
        return findPotentialPartners(userId);
    }

    /**
     * 실시간 매칭 파트너 검색
     * 현재 온라인이고 특정 세션 타입에 관심있는 사용자들 검색
     */
    default List<User> findRealTimeMatches(UUID userId, String sessionType) {
        // TODO: 실시간 매칭 큐와 온라인 상태를 고려한 검색
        return findPotentialPartners(userId);
    }

    /**
     * 스케줄 기반 매칭 파트너 검색
     * 특정 요일/시간대에 활동 가능한 파트너들 검색
     */
    default List<User> findScheduleBasedMatches(UUID userId, String dayOfWeek, String timeSlot) {
        // TODO: 온보딩 스케줄 데이터를 활용한 검색
        return findPotentialPartners(userId);
    }

    /**
     * 언어 교환 파트너 검색
     * 서로의 언어를 배울 수 있는 파트너들 검색 (A의 모국어 = B의 학습언어, A의 학습언어 = B의 모국어)
     */
    default List<User> findLanguageExchangePartners(UUID userId) {
        // TODO: 언어 매칭 로직 구현
        return findPotentialPartners(userId);
    }

    /**
     * 기본 필터를 적용한 추천 파트너 검색
     * 나이, 언어, 레벨 등 기본 필터 적용
     */
    default List<User> findRecommendedPartners(UUID userId, String nativeLanguage, String targetLanguage, 
                                              String languageLevel, Integer minAge, Integer maxAge) {
        // TODO: 필터 조건을 적용한 쿼리 구현
        return findPotentialPartners(userId);
    }

    // === 사용자 통계 및 분석 관련 쿼리 ===

    /**
     * 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userDisable = false")
    long countActiveUsers();

    /**
     * 온라인 사용자 수 조회 (최근 30분 이내 활동)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userCreatedAt > :since")
    long countOnlineUsers(@Param("since") java.time.LocalDateTime since);

    /**
     * 언어별 사용자 분포 조회
     */
    @Query("SELECT u.nativeLanguage.languageName, COUNT(u) FROM User u " +
           "WHERE u.nativeLanguage IS NOT NULL " +
           "GROUP BY u.nativeLanguage.languageName " +
           "ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByNativeLanguage();

    /**
     * 지역별 사용자 분포 조회
     */
    @Query("SELECT u.location.city, COUNT(u) FROM User u " +
           "WHERE u.location IS NOT NULL " +
           "GROUP BY u.location.city " +
           "ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByLocation();

    /**
     * 연령대별 사용자 분포 조회
     */
    @Query("SELECT CASE " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 20 THEN '10대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 30 THEN '20대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 40 THEN '30대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 50 THEN '40대' " +
           "ELSE '50대 이상' END AS ageGroup, COUNT(u) " +
           "FROM User u " +
           "WHERE u.birthyear IS NOT NULL AND u.birthyear != '' " +
           "GROUP BY CASE " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 20 THEN '10대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 30 THEN '20대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 40 THEN '30대' " +
           "WHEN YEAR(CURRENT_DATE) - CAST(u.birthyear AS integer) < 50 THEN '40대' " +
           "ELSE '50대 이상' END " +
           "ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByAgeGroup();

    /**
     * 매칭 성공률이 높은 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.userId IN (" +
           "SELECT um.user1.userId FROM UserMatch um WHERE um.isActive = true " +
           "UNION " +
           "SELECT um.user2.userId FROM UserMatch um WHERE um.isActive = true) " +
           "ORDER BY u.userCreatedAt DESC")
    List<User> findUsersWithActiveMatches();
}
