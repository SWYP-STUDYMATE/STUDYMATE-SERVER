package com.studymate.domain.analytics.service;

import com.studymate.domain.analytics.domain.dto.response.*;
import com.studymate.domain.analytics.domain.repository.*;
import com.studymate.domain.analytics.entity.*;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final SystemMetricsRepository systemMetricsRepository;
    private final AnalyticsNotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 총 XP 계산
        Integer totalXP = learningProgressRepository.getTotalXPByUserId(userId);
        
        // 현재 연속 학습 일수
        Integer currentStreak = learningProgressRepository.getCurrentStreakByUserId(userId);
        
        // 총 학습 시간
        Integer totalStudyTime = learningProgressRepository.getTotalStudyTimeByUserId(userId);
        
        // 최근 30일 학습 진도
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<LearningProgress> recentProgress = learningProgressRepository.findByUserIdAndDateRange(
                userId, startDate, endDate);
        
        // 언어별 진도
        Map<String, Integer> languageProgress = new HashMap<>();
        Set<String> languages = recentProgress.stream()
                .map(LearningProgress::getLanguageCode)
                .collect(Collectors.toSet());
        
        for (String language : languages) {
            Integer languageXP = learningProgressRepository.getTotalXPByUserIdAndLanguage(userId, language);
            languageProgress.put(language, languageXP != null ? languageXP : 0);
        }
        
        // 스킬별 학습 시간 계산
        Map<String, Integer> skillProgress = new HashMap<>();
        for (String language : languages) {
            Object[] skillTimes = learningProgressRepository.getSkillTimesByUserIdAndLanguage(userId, language);
            if (skillTimes != null && skillTimes.length >= 4) {
                skillProgress.put("SPEAKING", ((Number) skillTimes[0]).intValue());
                skillProgress.put("LISTENING", ((Number) skillTimes[1]).intValue());
                skillProgress.put("READING", ((Number) skillTimes[2]).intValue());
                skillProgress.put("WRITING", ((Number) skillTimes[3]).intValue());
                break; // 첫 번째 언어로만 계산
            }
        }
        
        // 일별 활동 기록
        List<UserStatsResponse.DailyProgress> dailyProgress = recentProgress.stream()
                .map(progress -> new UserStatsResponse.DailyProgress(
                        progress.getDate(),
                        progress.getXpEarned(),
                        progress.getTotalSessionMinutes(),
                        progress.getMessagesSent(),
                        progress.getTotalSessionMinutes() > 0 || progress.getMessagesSent() > 0
                ))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
        
        // 집계 통계
        int totalSessions = recentProgress.stream().mapToInt(LearningProgress::getSessionsCompleted).sum();
        int totalMessages = recentProgress.stream().mapToInt(LearningProgress::getMessagesSent).sum();
        int totalWords = recentProgress.stream().mapToInt(LearningProgress::getWordsLearned).sum();
        int totalTests = recentProgress.stream().mapToInt(LearningProgress::getTestsTaken).sum();
        int totalBadges = recentProgress.stream().mapToInt(LearningProgress::getBadgesEarned).sum();
        
        Double avgTestScore = learningProgressRepository.getAverageTestScoreByUserId(userId);
        
        // 성취 목록 (간단한 예시)
        List<UserStatsResponse.Achievement> achievements = generateAchievements(totalXP, currentStreak, totalSessions);
        
        return new UserStatsResponse(
                totalXP != null ? totalXP : 0,
                currentStreak != null ? currentStreak : 0,
                totalStudyTime != null ? totalStudyTime : 0,
                totalSessions,
                totalMessages,
                totalWords,
                totalTests,
                avgTestScore,
                totalBadges,
                languageProgress,
                skillProgress,
                dailyProgress,
                achievements
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStatsByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        // 기본 getUserStats와 유사하지만 날짜 범위 제한
        return getUserStats(userId); // 간단 구현, 실제로는 날짜 범위 적용 필요
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAnalyticsResponse getSystemAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = today.minusDays(7);
        LocalDateTime monthStart = today.minusDays(30);
        
        // 기본 통계
        Long totalUsers = userRepository.count();
        Long activeUsersToday = userActivityRepository.countDistinctActiveUsersByDate(now);
        Long activeUsersThisWeek = userActivityRepository.countDistinctActiveUsersByDate(weekStart);
        Long activeUsersThisMonth = userActivityRepository.countDistinctActiveUsersByDate(monthStart);
        
        // 시스템 메트릭에서 가져오기
        Long totalSessions = getSystemMetricValue("TOTAL_SESSIONS", 0L);
        Long completedSessionsToday = getSystemMetricValue("DAILY_COMPLETED_SESSIONS", 0L);
        Double averageSessionDuration = getSystemMetricValue("AVERAGE_SESSION_DURATION", 0.0);
        Long totalMessages = getSystemMetricValue("TOTAL_MESSAGES", 0L);
        Long messagesThisWeek = getSystemMetricValue("WEEKLY_MESSAGES", 0L);
        
        // 언어별 사용자 수
        List<Object[]> languageStats = learningProgressRepository.getLearnerCountByLanguage(
                now.toLocalDate().minusDays(30), now.toLocalDate());
        Map<String, Long> usersByLanguage = languageStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));
        
        // 트렌드 데이터 생성 (간단한 예시)
        List<SystemAnalyticsResponse.TrendData> userGrowthTrend = generateTrendData("USER_GROWTH", 30);
        List<SystemAnalyticsResponse.TrendData> activityTrend = generateTrendData("DAILY_ACTIVITY", 30);
        List<SystemAnalyticsResponse.TrendData> engagementTrend = generateTrendData("ENGAGEMENT", 30);
        
        // 시간별 활동 통계
        Map<Integer, Long> activityByHour = generateHourlyActivity();
        
        // 인기 언어
        List<SystemAnalyticsResponse.TopLanguage> topLanguages = generateTopLanguages(languageStats);
        
        // 시스템 상태
        SystemAnalyticsResponse.SystemHealthMetrics systemHealth = calculateSystemHealth();
        
        return new SystemAnalyticsResponse(
                totalUsers, activeUsersToday, activeUsersThisWeek, activeUsersThisMonth,
                totalSessions, completedSessionsToday, averageSessionDuration,
                totalMessages, messagesThisWeek, usersByLanguage, new HashMap<>(),
                userGrowthTrend, activityTrend, engagementTrend, activityByHour,
                topLanguages, systemHealth
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAnalyticsResponse getSystemAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // 날짜 범위 적용한 시스템 분석 (간단 구현)
        return getSystemAnalytics();
    }

    @Override
    public void recordUserActivity(UUID userId, String activityType, String activityCategory,
                                  String description, String metadata, String ipAddress, String userAgent) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                UserActivity activity = UserActivity.builder()
                        .user(user)
                        .activityType(activityType)
                        .activityCategory(activityCategory)
                        .description(description)
                        .metadata(metadata)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .build();
                
                userActivityRepository.save(activity);
                
                // 학습 진도에도 반영 (필요한 경우)
                updateLearningProgressFromActivity(userId, activityType, metadata);
            }
        } catch (Exception e) {
            log.error("Failed to record user activity: userId={}, activityType={}", userId, activityType, e);
        }
    }

    @Override
    public void updateLearningProgress(UUID userId, String languageCode, String progressType,
                                     Integer value, String metadata) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            LocalDate today = LocalDate.now();
            Optional<LearningProgress> existingProgress = learningProgressRepository
                    .findByUserAndDateAndLanguageCode(user, today, languageCode);
            
            LearningProgress progress = existingProgress.orElseGet(() -> 
                    LearningProgress.builder()
                            .user(user)
                            .date(today)
                            .languageCode(languageCode)
                            .build());
            
            // 진도 유형에 따라 업데이트
            switch (progressType.toUpperCase()) {
                case "SESSION_COMPLETED":
                    progress.addSessionCompleted(value, metadata);
                    break;
                case "MESSAGE_SENT":
                    progress.addMessageSent(value);
                    break;
                case "WORDS_LEARNED":
                    progress.addWordsLearned(value);
                    break;
                case "TEST_TAKEN":
                    progress.addTestTaken(value.doubleValue());
                    break;
                case "BADGE_EARNED":
                    progress.addBadgeEarned();
                    break;
                case "XP_EARNED":
                    progress.addXP(value);
                    break;
            }
            
            learningProgressRepository.save(progress);
        } catch (Exception e) {
            log.error("Failed to update learning progress: userId={}, progressType={}", userId, progressType, e);
        }
    }

    @Override
    public void recordSystemMetric(String metricName, String metricCategory, Double metricValue,
                                  String metricUnit, String aggregationPeriod) {
        try {
            SystemMetrics metric = SystemMetrics.builder()
                    .metricName(metricName)
                    .metricCategory(metricCategory)
                    .metricValue(metricValue)
                    .metricUnit(metricUnit)
                    .date(LocalDateTime.now())
                    .aggregationPeriod(aggregationPeriod)
                    .build();
            
            systemMetricsRepository.save(metric);
        } catch (Exception e) {
            log.error("Failed to record system metric: metricName={}", metricName, e);
        }
    }

    @Override
    public void calculateDailyMetrics(LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            // 일별 활성 사용자 수
            Long dailyActiveUsers = userActivityRepository.countDistinctActiveUsersByDate(startOfDay);
            recordSystemMetric("DAILY_ACTIVE_USERS", "USER", dailyActiveUsers.doubleValue(), "COUNT", "DAILY");
            
            // 일별 세션 수
            List<UserActivity> sessionActivities = userActivityRepository.findByActivityTypeAndDateRange(
                    "SESSION_COMPLETED", startOfDay, endOfDay);
            recordSystemMetric("DAILY_SESSIONS", "SESSION", (double) sessionActivities.size(), "COUNT", "DAILY");
            
            // 일별 메시지 수
            List<UserActivity> messageActivities = userActivityRepository.findByActivityTypeAndDateRange(
                    "MESSAGE_SENT", startOfDay, endOfDay);
            recordSystemMetric("DAILY_MESSAGES", "CHAT", (double) messageActivities.size(), "COUNT", "DAILY");
            
        } catch (Exception e) {
            log.error("Failed to calculate daily metrics for date: {}", date, e);
        }
    }

    @Override
    public void calculateWeeklyMetrics(LocalDate weekStart) {
        // 주간 메트릭 계산 구현
    }

    @Override
    public void calculateMonthlyMetrics(LocalDate monthStart) {
        // 월간 메트릭 계산 구현
    }

    // Helper methods
    
    private void updateLearningProgressFromActivity(UUID userId, String activityType, String metadata) {
        // 활동 타입에 따라 학습 진도 업데이트
        switch (activityType.toUpperCase()) {
            case "SESSION_COMPLETED":
                updateLearningProgress(userId, "en", "SESSION_COMPLETED", 60, "SPEAKING");
                break;
            case "MESSAGE_SENT":
                updateLearningProgress(userId, "en", "MESSAGE_SENT", 1, null);
                break;
            // 추가 케이스들...
        }
    }
    
    private Long getSystemMetricValue(String metricName, Long defaultValue) {
        return systemMetricsRepository.findTopByMetricNameOrderByDateDesc(metricName)
                .map(metric -> metric.getMetricValue().longValue())
                .orElse(defaultValue);
    }
    
    private Double getSystemMetricValue(String metricName, Double defaultValue) {
        return systemMetricsRepository.findTopByMetricNameOrderByDateDesc(metricName)
                .map(SystemMetrics::getMetricValue)
                .orElse(defaultValue);
    }
    
    private List<UserStatsResponse.Achievement> generateAchievements(Integer totalXP, Integer streak, Integer sessions) {
        List<UserStatsResponse.Achievement> achievements = new ArrayList<>();
        
        if (totalXP != null && totalXP >= 1000) {
            achievements.add(new UserStatsResponse.Achievement(
                    "XP Master", "Earned 1000+ XP", LocalDate.now(), "/badges/xp-master.png"));
        }
        
        if (streak != null && streak >= 7) {
            achievements.add(new UserStatsResponse.Achievement(
                    "Week Warrior", "7-day learning streak", LocalDate.now(), "/badges/streak-7.png"));
        }
        
        if (sessions >= 10) {
            achievements.add(new UserStatsResponse.Achievement(
                    "Session Pro", "Completed 10+ sessions", LocalDate.now(), "/badges/session-pro.png"));
        }
        
        return achievements;
    }
    
    private List<SystemAnalyticsResponse.TrendData> generateTrendData(String metricName, int days) {
        List<SystemAnalyticsResponse.TrendData> trendData = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        // 실제 구현에서는 시스템 메트릭에서 데이터를 가져와야 함
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            double value = Math.random() * 100; // 샘플 데이터
            trendData.add(new SystemAnalyticsResponse.TrendData(date, value, metricName));
        }
        
        return trendData;
    }
    
    private Map<Integer, Long> generateHourlyActivity() {
        Map<Integer, Long> hourlyActivity = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        
        List<Object[]> hourlyStats = userActivityRepository.getHourlyActivityStats(dayStart, dayEnd);
        
        for (Object[] stat : hourlyStats) {
            Integer hour = (Integer) stat[0];
            Long count = ((Number) stat[1]).longValue();
            hourlyActivity.put(hour, count);
        }
        
        return hourlyActivity;
    }
    
    private List<SystemAnalyticsResponse.TopLanguage> generateTopLanguages(List<Object[]> languageStats) {
        return languageStats.stream()
                .map(stat -> new SystemAnalyticsResponse.TopLanguage(
                        (String) stat[0],
                        getLanguageName((String) stat[0]),
                        ((Number) stat[1]).longValue(),
                        0L, // 세션 수는 별도 계산 필요
                        0.0  // 평균 진도는 별도 계산 필요
                ))
                .collect(Collectors.toList());
    }
    
    private String getLanguageName(String languageCode) {
        Map<String, String> languageNames = Map.of(
                "en", "English",
                "ko", "Korean",
                "ja", "Japanese",
                "zh", "Chinese",
                "es", "Spanish",
                "fr", "French"
        );
        return languageNames.getOrDefault(languageCode, languageCode);
    }
    
    private SystemAnalyticsResponse.SystemHealthMetrics calculateSystemHealth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        
        // 성공률 계산
        List<Object[]> successStats = userActivityRepository.getSuccessStatsByDateRange(dayStart, now);
        double successRate = calculateSuccessRate(successStats);
        
        // 에러 수 계산
        List<UserActivity> errorActivities = userActivityRepository.findErrorActivitiesByDateRange(dayStart, now);
        long errorCount = errorActivities.size();
        
        String systemStatus = determineSystemStatus(successRate, errorCount);
        
        return new SystemAnalyticsResponse.SystemHealthMetrics(
                successRate, 0.0, errorCount, systemStatus);
    }
    
    private double calculateSuccessRate(List<Object[]> successStats) {
        long totalSuccess = 0;
        long totalFailure = 0;
        
        for (Object[] stat : successStats) {
            Boolean isSuccess = (Boolean) stat[0];
            Long count = ((Number) stat[1]).longValue();
            
            if (isSuccess) {
                totalSuccess += count;
            } else {
                totalFailure += count;
            }
        }
        
        long total = totalSuccess + totalFailure;
        return total > 0 ? (double) totalSuccess / total * 100 : 100.0;
    }
    
    private String determineSystemStatus(double successRate, long errorCount) {
        if (successRate >= 99.0 && errorCount < 10) {
            return "HEALTHY";
        } else if (successRate >= 95.0 && errorCount < 50) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }
}