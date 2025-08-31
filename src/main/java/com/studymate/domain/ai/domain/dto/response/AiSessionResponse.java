package com.studymate.domain.ai.domain.dto.response;

import com.studymate.domain.ai.entity.AiSession;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AiSessionResponse {
    
    private UUID id;
    private UUID userId;
    private UUID aiPartnerId;
    private String aiPartnerName;
    private String aiPartnerAvatar;
    private String sessionTitle;
    private AiSession.SessionType sessionType;
    private AiSession.SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private Integer messageCount;
    private Integer userRating;
    private String userFeedback;
    private String learningObjectives;
    private String sessionSummary;
    private String improvementSuggestions;
    private List<String> vocabularyLearned;
    private List<String> grammarPoints;
    private List<AiMessageResponse> recentMessages;
    private LearningProgress learningProgress;
    
    @Data
    @Builder
    public static class LearningProgress {
        private Double accuracyScore;
        private Integer correctionCount;
        private Integer vocabularyCount;
        private Double engagementScore;
        private String overallFeedback;
    }
}