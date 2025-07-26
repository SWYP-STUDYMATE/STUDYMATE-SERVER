package com.studymate.domain.onboarding.domain.dto.request;

import java.util.List;
import java.util.UUID;

public record LanguageLevelRequest(
        UUID userId,
        List<LanguageLevelDto> languages

) {
    public static record LanguageLevelDto(
            int languageId,
            int langLevelTypeId
    ) {}
}
