package com.studymate.domain.onboard.domain.dto.request;

import java.util.List;
import java.util.UUID;

public record LanguageLevelRequest(
        List<LanguageLevelDto> languages

) {
    public static record LanguageLevelDto(
            int languageId,
            int currentLevelId,
            int targetLevelId
    ) {}
}
