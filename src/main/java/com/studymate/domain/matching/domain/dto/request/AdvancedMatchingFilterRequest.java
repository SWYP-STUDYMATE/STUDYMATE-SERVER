package com.studymate.domain.matching.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedMatchingFilterRequest {
    
    // 기본 필터
    private String nativeLanguage;
    private String targetLanguage;
    private String languageLevel;
    
    // 연령 필터
    @Min(value = 18, message = "최소 연령은 18세입니다")
    @Max(value = 100, message = "최대 연령은 100세입니다")
    private Integer minAge;
    
    @Min(value = 18, message = "최소 연령은 18세입니다") 
    @Max(value = 100, message = "최대 연령은 100세입니다")
    private Integer maxAge;
    
    // 위치 필터
    private String country;
    private String city;
    private List<String> cities;
    
    // 성별 필터
    private String gender; // MALE, FEMALE, ANY
    
    // 온라인 상태 필터
    private Boolean onlineOnly = false;
    private Boolean studyingOnly = false;
    
    // 호환성 점수 필터
    @Min(value = 0, message = "최소 호환성 점수는 0입니다")
    @Max(value = 100, message = "최대 호환성 점수는 100입니다") 
    private Double minCompatibilityScore;
    
    // 학습 목표 필터
    private List<String> studyGoals;
    
    // 성격 필터
    private List<String> personalities;
    
    // 관심사 필터
    private List<String> topics;
    
    // 파트너 선호도 필터
    private String partnerGender;
    private List<String> communicationMethods;
    
    // 일정 필터
    private List<String> availableDays; // MONDAY, TUESDAY, etc.
    private String preferredTime; // MORNING, AFTERNOON, EVENING, NIGHT
    
    // 세션 경험 필터
    @Min(value = 0, message = "최소 세션 수는 0입니다")
    private Integer minSessionsCompleted;
    
    // 가입 기간 필터
    private Integer maxDaysSinceJoined;
    private Integer minDaysSinceJoined;
    
    // 최근 활동 필터 (일 단위)
    @Min(value = 0, message = "최근 활동 일수는 0 이상이어야 합니다")
    private Integer maxDaysInactive;
    
    // 정렬 옵션
    private String sortBy = "compatibility"; // compatibility, lastActive, joinDate, sessionCount
    private String sortDirection = "desc"; // asc, desc
    
    // 결과 제한
    @Min(value = 1, message = "최소 결과 수는 1입니다")
    @Max(value = 100, message = "최대 결과 수는 100입니다")
    private Integer limit = 20;
    
    // 제외할 사용자 목록 (이미 매칭 요청 보낸 사용자 등)
    private List<String> excludeUserIds;
    
    // Helper methods
    public boolean hasAgeFilter() {
        return minAge != null || maxAge != null;
    }
    
    public boolean hasLocationFilter() {
        return country != null || city != null || (cities != null && !cities.isEmpty());
    }
    
    public boolean hasPersonalityFilter() {
        return personalities != null && !personalities.isEmpty();
    }
    
    public boolean hasTopicFilter() {
        return topics != null && !topics.isEmpty();
    }
    
    public boolean hasStudyGoalFilter() {
        return studyGoals != null && !studyGoals.isEmpty();
    }
    
    public boolean hasScheduleFilter() {
        return (availableDays != null && !availableDays.isEmpty()) || preferredTime != null;
    }
}