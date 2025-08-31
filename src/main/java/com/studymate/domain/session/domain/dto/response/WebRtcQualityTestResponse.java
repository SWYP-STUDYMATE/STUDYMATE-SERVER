package com.studymate.domain.session.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcQualityTestResponse {
    private UUID userId;
    private Integer overallScore;
    private String qualityLevel;
    private Boolean isRecommendedForVideo;
    private Integer latencyScore;
    private Integer bandwidthScore;
    private Integer packetLossScore;
    private Integer jitterScore;
    private List<String> recommendations;
}