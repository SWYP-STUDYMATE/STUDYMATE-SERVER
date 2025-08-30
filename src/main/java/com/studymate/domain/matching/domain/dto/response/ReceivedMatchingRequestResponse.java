package com.studymate.domain.matching.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedMatchingRequestResponse {
    private UUID requestId;
    private UUID senderUserId;
    private String senderUserName;
    private String senderUserProfileImage;
    private String senderUserLocation;
    private String senderUserNativeLanguage;
    private String message;
    private String status;
    private LocalDateTime receivedAt;
    private double compatibilityScore;
}