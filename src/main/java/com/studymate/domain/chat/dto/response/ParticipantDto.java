package com.studymate.domain.chat.dto.response;

import java.util.UUID;

public interface ParticipantDto {
    UUID getUserId();
    String getName();
    String getProfileImage();
}