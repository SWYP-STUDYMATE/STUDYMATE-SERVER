package com.studymate.domain.session.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcQualityTestRequest {
    private UUID userId;
    private String deviceType;
    private String browserType;
    private Boolean hasCamera;
    private Boolean hasMicrophone;
    private Integer latency;
    private Double bandwidth;
    private Double packetLoss;
    private Integer jitter;
}