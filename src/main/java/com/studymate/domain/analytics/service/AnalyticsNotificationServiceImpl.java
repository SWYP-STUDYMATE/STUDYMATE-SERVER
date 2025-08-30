package com.studymate.domain.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.analytics.domain.dto.response.SystemAnalyticsResponse;
import com.studymate.domain.analytics.domain.dto.response.UserStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsNotificationServiceImpl implements AnalyticsNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void notifyUserStatsUpdate(UUID userId, UserStatsResponse stats) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "STATS_UPDATE");
            notification.put("timestamp", LocalDateTime.now());
            notification.put("data", stats);
            
            String destination = "/topic/users/" + userId + "/stats";
            messagingTemplate.convertAndSend(destination, notification);
            
            log.debug("Sent stats update to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send stats update notification to user {}", userId, e);
        }
    }

    @Override
    public void notifySystemAnalyticsUpdate(SystemAnalyticsResponse analytics) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SYSTEM_ANALYTICS_UPDATE");
            notification.put("timestamp", LocalDateTime.now());
            notification.put("data", analytics);
            
            messagingTemplate.convertAndSend("/topic/admin/analytics", notification);
            
            log.debug("Sent system analytics update to admins");
        } catch (Exception e) {
            log.error("Failed to send system analytics update notification", e);
        }
    }

    @Override
    public void notifyActivityFeedback(UUID userId, String activityType, String message, Object data) {
        try {
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("type", "ACTIVITY_FEEDBACK");
            feedback.put("activityType", activityType);
            feedback.put("message", message);
            feedback.put("data", data);
            feedback.put("timestamp", LocalDateTime.now());
            
            String destination = "/topic/users/" + userId + "/feedback";
            messagingTemplate.convertAndSend(destination, feedback);
            
            log.debug("Sent activity feedback to user {}: {}", userId, activityType);
        } catch (Exception e) {
            log.error("Failed to send activity feedback to user {}", userId, e);
        }
    }

    @Override
    public void notifySystemAlert(String alertType, String message, String severity) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "SYSTEM_ALERT");
            alert.put("alertType", alertType);
            alert.put("message", message);
            alert.put("severity", severity);
            alert.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSend("/topic/admin/alerts", alert);
            
            log.warn("System alert sent: {} - {} [{}]", alertType, message, severity);
        } catch (Exception e) {
            log.error("Failed to send system alert notification", e);
        }
    }

    @Override
    public void notifyGoalAchievement(UUID userId, String goalType, String achievement) {
        try {
            Map<String, Object> achievementData = new HashMap<>();
            achievementData.put("type", "GOAL_ACHIEVEMENT");
            achievementData.put("goalType", goalType);
            achievementData.put("achievement", achievement);
            achievementData.put("timestamp", LocalDateTime.now());
            
            // 사용자별 알림
            String userDestination = "/topic/users/" + userId + "/achievements";
            messagingTemplate.convertAndSend(userDestination, achievementData);
            
            // 전체 사용자에게 공개 알림 (레벨업, 특별 성취 등)
            if (isPublicAchievement(goalType)) {
                achievementData.put("userId", userId);
                messagingTemplate.convertAndSend("/topic/public/achievements", achievementData);
            }
            
            log.info("Goal achievement notification sent to user {}: {} - {}", userId, goalType, achievement);
        } catch (Exception e) {
            log.error("Failed to send goal achievement notification to user {}", userId, e);
        }
    }

    /**
     * XP 획득 알림
     */
    public void notifyXpEarned(UUID userId, int xpAmount, String source) {
        Map<String, Object> xpData = new HashMap<>();
        xpData.put("xpAmount", xpAmount);
        xpData.put("source", source);
        xpData.put("totalXp", getCurrentUserXp(userId));
        
        notifyActivityFeedback(userId, "XP_EARNED", 
                String.format("XP %d 획득! (%s)", xpAmount, source), xpData);
    }

    /**
     * 레벨업 알림
     */
    public void notifyLevelUp(UUID userId, int newLevel, int newLevelXp) {
        Map<String, Object> levelData = new HashMap<>();
        levelData.put("newLevel", newLevel);
        levelData.put("newLevelXp", newLevelXp);
        
        notifyActivityFeedback(userId, "LEVEL_UP", 
                String.format("축하합니다! 레벨 %d 달성!", newLevel), levelData);
        
        // 레벨업은 공개 성취로 처리
        notifyGoalAchievement(userId, "LEVEL_UP", String.format("레벨 %d 달성", newLevel));
    }

    /**
     * 연속 학습 기록 알림
     */
    public void notifyStreak(UUID userId, int streakDays) {
        Map<String, Object> streakData = new HashMap<>();
        streakData.put("streakDays", streakDays);
        
        String message = streakDays == 1 ? 
                "학습 시작! 연속 학습 기록을 쌓아보세요!" : 
                String.format("연속 %d일째 학습 중! 대단해요!", streakDays);
                
        notifyActivityFeedback(userId, "STREAK_UPDATE", message, streakData);
        
        // 특정 연속 기록 마일스톤은 성취로 처리
        if (streakDays % 7 == 0 && streakDays > 0) {
            notifyGoalAchievement(userId, "STREAK_MILESTONE", 
                    String.format("연속 %d일 학습 달성", streakDays));
        }
    }

    /**
     * 일일 목표 달성 알림
     */
    public void notifyDailyGoalComplete(UUID userId, String goalType, int targetValue, int achievedValue) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("goalType", goalType);
        goalData.put("targetValue", targetValue);
        goalData.put("achievedValue", achievedValue);
        goalData.put("completionRate", Math.min(100.0, (double) achievedValue / targetValue * 100));
        
        String message = String.format("일일 %s 목표 달성! (%d/%d)", 
                getGoalTypeKoreanName(goalType), achievedValue, targetValue);
                
        notifyActivityFeedback(userId, "DAILY_GOAL_COMPLETE", message, goalData);
        notifyGoalAchievement(userId, "DAILY_GOAL", message);
    }

    /**
     * 실시간 시스템 상태 업데이트
     */
    public void notifySystemStatusUpdate(String status, Map<String, Object> metrics) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "SYSTEM_STATUS_UPDATE");
            statusUpdate.put("status", status);
            statusUpdate.put("metrics", metrics);
            statusUpdate.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSend("/topic/admin/system-status", statusUpdate);
            
            log.debug("System status update sent: {}", status);
        } catch (Exception e) {
            log.error("Failed to send system status update", e);
        }
    }

    // Helper methods

    private boolean isPublicAchievement(String goalType) {
        return goalType.equals("LEVEL_UP") || 
               goalType.equals("STREAK_MILESTONE") ||
               goalType.equals("SPECIAL_ACHIEVEMENT");
    }

    private int getCurrentUserXp(UUID userId) {
        // 실제 구현에서는 AnalyticsService에서 현재 XP 조회
        // 임시로 0 반환
        return 0;
    }

    private String getGoalTypeKoreanName(String goalType) {
        Map<String, String> goalTypeNames = Map.of(
                "STUDY_TIME", "학습 시간",
                "MESSAGES_SENT", "메시지 전송",
                "SESSIONS_COMPLETED", "세션 완료",
                "WORDS_LEARNED", "단어 학습",
                "TESTS_TAKEN", "테스트 완료"
        );
        return goalTypeNames.getOrDefault(goalType, goalType);
    }
}