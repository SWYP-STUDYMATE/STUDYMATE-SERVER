package com.studymate.domain.matching.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordFeedbackRequest {

    @NotNull(message = "파트너 ID는 필수입니다.")
    private UUID partnerId;

    @NotNull(message = "품질 점수는 필수입니다.")
    @Min(value = 1, message = "품질 점수는 1 이상이어야 합니다.")
    @Max(value = 5, message = "품질 점수는 5 이하여야 합니다.")
    private Integer qualityScore;

    @Size(max = 1000, message = "피드백은 1000자를 초과할 수 없습니다.")
    private String feedback;

    @Min(value = 1, message = "소통 능력 평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "소통 능력 평점은 5 이하여야 합니다.")
    private Integer communicationRating;

    @Min(value = 1, message = "언어 수준 평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "언어 수준 평점은 5 이하여야 합니다.")
    private Integer languageLevelRating;

    @Min(value = 1, message = "가르치는 능력 평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "가르치는 능력 평점은 5 이하여야 합니다.")
    private Integer teachingAbilityRating;

    @Min(value = 1, message = "인내심 평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "인내심 평점은 5 이하여야 합니다.")
    private Integer patienceRating;

    @Min(value = 1, message = "시간 약속 준수 평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "시간 약속 준수 평점은 5 이하여야 합니다.")
    private Integer punctualityRating;

    @Size(max = 500, message = "텍스트 피드백은 500자를 초과할 수 없습니다.")
    private String writtenFeedback;

    @Min(value = 1, message = "세션 품질 점수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "세션 품질 점수는 100 이하여야 합니다.")
    private Integer sessionQualityScore;

    private Boolean wouldMatchAgain;

    @Size(max = 2000, message = "문제점 리스트는 2000자를 초과할 수 없습니다.")
    private String reportedIssues; // JSON 형태의 문제점 리스트

    @Size(max = 1000, message = "개선 제안은 1000자를 초과할 수 없습니다.")
    private String suggestedImprovements;
}