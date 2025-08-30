package com.studymate.domain.matching.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matching_feedback")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer; // 피드백을 주는 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private User partner; // 피드백 받는 파트너

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private UserMatch userMatch; // 관련 매칭 정보

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 1-5 점

    @Column(name = "communication_rating")
    private Integer communicationRating; // 소통 능력

    @Column(name = "language_level_rating")
    private Integer languageLevelRating; // 언어 수준 적절성

    @Column(name = "teaching_ability_rating")
    private Integer teachingAbilityRating; // 가르치는 능력

    @Column(name = "patience_rating")
    private Integer patienceRating; // 인내심

    @Column(name = "punctuality_rating")
    private Integer punctualityRating; // 시간 약속 준수

    @Column(name = "written_feedback", columnDefinition = "TEXT")
    private String writtenFeedback; // 텍스트 피드백

    @Column(name = "session_quality_score")
    private Integer sessionQualityScore; // 세션 품질 점수

    @Column(name = "would_match_again")
    private Boolean wouldMatchAgain; // 다시 매칭하고 싶은지

    @Column(name = "reported_issues", columnDefinition = "JSON")
    private String reportedIssues; // JSON 형태의 문제점 리스트

    @Column(name = "suggested_improvements", columnDefinition = "TEXT")
    private String suggestedImprovements; // 개선 제안

    public double calculateAverageRating() {
        int count = 0;
        int total = 0;
        
        if (communicationRating != null) { total += communicationRating; count++; }
        if (languageLevelRating != null) { total += languageLevelRating; count++; }
        if (teachingAbilityRating != null) { total += teachingAbilityRating; count++; }
        if (patienceRating != null) { total += patienceRating; count++; }
        if (punctualityRating != null) { total += punctualityRating; count++; }
        
        return count > 0 ? (double) total / count : 0.0;
    }

    public boolean isPositiveFeedback() {
        return overallRating != null && overallRating >= 4;
    }

    public boolean hasIssues() {
        return reportedIssues != null && !reportedIssues.trim().isEmpty();
    }
}