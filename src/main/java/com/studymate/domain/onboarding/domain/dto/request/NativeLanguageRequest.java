package com.studymate.domain.onboarding.domain.dto.request;

import java.util.UUID;

public record NativeLanguageRequest(
        UUID userId,
        int languageId
) {
}
