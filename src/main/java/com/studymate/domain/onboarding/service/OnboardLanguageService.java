package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.LanguageLevelRequest;
import com.studymate.domain.onboarding.domain.dto.request.NativeLanguageRequest;
import com.studymate.domain.onboarding.domain.dto.response.LangLevelTypeResponse;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;

import java.util.List;
import java.util.UUID;

public interface OnboardLanguageService {
    void saveNativeLanguage (UUID userId,NativeLanguageRequest req);
    void saveLanguageLevel(UUID userId,LanguageLevelRequest req);
    List<LanguageResponse> getAllLanguages();
    List<LangLevelTypeResponse> getLanguageLevelTypes() ;
    List<LangLevelTypeResponse> getPartnerLevelTypes();
}
