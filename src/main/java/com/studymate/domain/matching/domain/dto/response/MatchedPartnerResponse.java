package com.studymate.domain.matching.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchedPartnerResponse {
    private UUID matchId;
    private UUID partnerUserId;
    private String partnerUserName;
    private String partnerUserProfileImage;
    private String partnerUserLocation;
    private String partnerUserNativeLanguage;
    private String partnerUserBio;
    private LocalDateTime matchedAt;
    private double compatibilityScore;
    private String onlineStatus;
    private String lastActiveTime;
    private int totalSessionsCompleted;
    private String favoriteTopics;
}