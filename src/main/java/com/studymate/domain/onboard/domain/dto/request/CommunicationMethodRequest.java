package com.studymate.domain.onboard.domain.dto.request;

import com.studymate.domain.onboard.domain.type.CommunicationMethodType;

import java.util.UUID;

public record CommunicationMethodRequest(
        CommunicationMethodType communicationMethodType
) {
}
