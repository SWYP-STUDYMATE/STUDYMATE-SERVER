package com.studymate.domain.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingAlternativeResponse {
    private List<Alternative> alternatives;
    private String recommendedAlternative;
    private String reason;
    private Integer estimatedWaitTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alternative {
        private String type;
        private String title;
        private String description;
        private Integer priority;
        private Boolean available;
    }
}