package com.studymate.domain.onboarding.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingDataResponse {
    
    // 사용자의 현재 온보딩 데이터
    private UserOnboardingData userOnboardingData;
    
    // 선택 가능한 옵션들
    private OnboardingOptions availableOptions;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserOnboardingData {
        // 언어 정보
        private Integer nativeLanguageId;
        private List<SelectedTargetLanguage> targetLanguages;
        
        // 관심사 정보
        private List<Integer> motivationIds;
        private List<Integer> topicIds;
        private List<Integer> learningStyleIds;
        private List<Integer> learningExpectationIds;
        
        // 파트너 선호도
        private List<Integer> partnerPersonalityIds;
        private List<Integer> groupSizeIds;
        
        // 스케줄 정보
        private List<Integer> scheduleIds;
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SelectedTargetLanguage {
            private Integer languageId;
            private String languageName;
            private Integer currentLevelId;
            private String currentLevelName;
            private Integer targetLevelId;
            private String targetLevelName;
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OnboardingOptions {
        private List<LanguageOption> languages;
        private List<LevelOption> levels;
        private List<MotivationOption> motivations;
        private List<TopicOption> topics;
        private List<LearningStyleOption> learningStyles;
        private List<LearningExpectationOption> learningExpectations;
        private List<PartnerPersonalityOption> partnerPersonalities;
        private List<GroupSizeOption> groupSizes;
        private List<ScheduleOption> schedules;
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LanguageOption {
            private Integer id;
            private String name;
            private String code;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LevelOption {
            private Integer id;
            private String name;
            private String description;
            private String category;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MotivationOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TopicOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LearningStyleOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LearningExpectationOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PartnerPersonalityOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class GroupSizeOption {
            private Integer id;
            private String name;
            private String description;
        }
        
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ScheduleOption {
            private Integer id;
            private String name;
            private String timeSlot;
            private String dayOfWeek;
        }
    }
}