package com.studymate.domain.matching.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingAlternativeRequest {
    private UUID userId;
    private String failureReason;
    private Integer searchDuration;
    private Integer attemptCount;
    private LocalDateTime lastAttemptTime;
}