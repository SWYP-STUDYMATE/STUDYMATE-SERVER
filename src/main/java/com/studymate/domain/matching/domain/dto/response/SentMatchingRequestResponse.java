package com.studymate.domain.matching.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentMatchingRequestResponse {
    private UUID requestId;
    private UUID targetUserId;
    private String targetUserName;
    private String targetUserProfileImage;
    private String targetUserLocation;
    private String targetUserNativeLanguage;
    private String message;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime responseAt;
}