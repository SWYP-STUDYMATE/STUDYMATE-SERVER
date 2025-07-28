package com.studymate.domain.onboarding.domain.dto.request;

import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;

import java.util.UUID;

public record CommunicationMethodRequest(
        CommunicationMethodType communicationMethodType
) {
}
