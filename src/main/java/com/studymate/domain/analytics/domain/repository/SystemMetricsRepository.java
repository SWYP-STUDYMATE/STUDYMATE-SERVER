package com.studymate.domain.analytics.domain.repository;

import com.studymate.domain.analytics.entity.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {

    // 특정 메트릭의 최신 값 조회
    Optional<SystemMetrics> findTopByMetricNameOrderByDateDesc(String metricName);

    // 메트릭 이름과 기간으로 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.metricName = :metricName AND sm.date BETWEEN :startDate AND :endDate ORDER BY sm.date ASC")
    List<SystemMetrics> findByMetricNameAndDateRange(@Param("metricName") String metricName,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // 카테고리별 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.metricCategory = :category AND sm.date BETWEEN :startDate AND :endDate ORDER BY sm.date ASC")
    List<SystemMetrics> findByCategoryAndDateRange(@Param("category") String category,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // 집계 기간별 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.aggregationPeriod = :period AND sm.date BETWEEN :startDate AND :endDate ORDER BY sm.date ASC")
    List<SystemMetrics> findByAggregationPeriodAndDateRange(@Param("period") String period,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    // 일별 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.aggregationPeriod = 'DAILY' AND DATE(sm.date) = DATE(:date) ORDER BY sm.metricName")
    List<SystemMetrics> findDailyMetricsByDate(@Param("date") LocalDateTime date);

    // 주간 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.aggregationPeriod = 'WEEKLY' AND WEEK(sm.date) = WEEK(:date) AND YEAR(sm.date) = YEAR(:date) ORDER BY sm.metricName")
    List<SystemMetrics> findWeeklyMetricsByDate(@Param("date") LocalDateTime date);

    // 월간 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.aggregationPeriod = 'MONTHLY' AND MONTH(sm.date) = MONTH(:date) AND YEAR(sm.date) = YEAR(:date) ORDER BY sm.metricName")
    List<SystemMetrics> findMonthlyMetricsByDate(@Param("date") LocalDateTime date);

    // 메트릭 이름 목록 조회
    @Query("SELECT DISTINCT sm.metricName FROM SystemMetrics sm WHERE sm.metricCategory = :category")
    List<String> findDistinctMetricNamesByCategory(@Param("category") String category);

    // 카테고리 목록 조회
    @Query("SELECT DISTINCT sm.metricCategory FROM SystemMetrics sm")
    List<String> findDistinctCategories();

    // 최고값 조회
    @Query("SELECT MAX(sm.metricValue) FROM SystemMetrics sm WHERE sm.metricName = :metricName AND sm.date BETWEEN :startDate AND :endDate")
    Optional<Double> findMaxValueByMetricNameAndDateRange(@Param("metricName") String metricName,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // 최저값 조회
    @Query("SELECT MIN(sm.metricValue) FROM SystemMetrics sm WHERE sm.metricName = :metricName AND sm.date BETWEEN :startDate AND :endDate")
    Optional<Double> findMinValueByMetricNameAndDateRange(@Param("metricName") String metricName,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // 평균값 조회 (임시 구현)
    default Optional<Double> findAvgValueByMetricNameAndDateRange(String metricName,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        // TODO: 실제 평균값 계산 로직 구현 필요
        return Optional.of(50.0); // 임시로 50.0 반환
    }

    // 성장률 계산용 데이터 조회
    @Query("SELECT sm.metricValue, sm.date FROM SystemMetrics sm WHERE sm.metricName = :metricName " +
           "AND sm.aggregationPeriod = :period ORDER BY sm.date DESC LIMIT 2")
    List<Object[]> findLatestTwoValuesByMetricNameAndPeriod(@Param("metricName") String metricName,
                                                           @Param("period") String period);

    // 시간별 트렌드 데이터 (임시 구현)
    default List<Object[]> getTrendDataByMetricName(String metricName,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate) {
        // TODO: 실제 트렌드 데이터 조회 로직 구현 필요
        return java.util.Collections.emptyList();
    }

    // 특정 태그를 가진 메트릭 조회
    @Query("SELECT sm FROM SystemMetrics sm WHERE sm.tags LIKE %:tag% AND sm.date BETWEEN :startDate AND :endDate ORDER BY sm.date ASC")
    List<SystemMetrics> findByTagAndDateRange(@Param("tag") String tag,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}