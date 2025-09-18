package com.studymate.domain.user.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class UserLanguageInfoResponse {
    private LanguageInfo nativeLanguage;
    @Builder.Default
    private List<TargetLanguageInfo> targetLanguages = Collections.emptyList();

    @Getter
    @Builder
    public static class LanguageInfo {
        private Integer languageId;
        private String languageName;
    }

    @Getter
    @Builder
    public static class TargetLanguageInfo {
        private Integer languageId;
        private String languageName;
        private Integer currentLevelId;
        private String currentLevelName;
        private Integer targetLevelId;
        private String targetLevelName;
    }
}
