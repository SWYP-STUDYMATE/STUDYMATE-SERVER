package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.LanguageLevelRequest;
import com.studymate.domain.onboarding.domain.dto.request.NativeLanguageRequest;
import com.studymate.domain.onboarding.domain.dto.response.LangLevelTypeResponse;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;
import com.studymate.domain.onboarding.domain.repository.LangLevelTypeRepository;
import com.studymate.domain.onboarding.domain.repository.LanguageRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardLangLevelRepository;
import com.studymate.domain.onboarding.entity.LangLevelType;
import com.studymate.domain.onboarding.entity.Language;
import com.studymate.domain.onboarding.entity.OnboardLangLevel;
import com.studymate.domain.onboarding.entity.OnboardLangLevelId;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardLanguageServiceImpl implements OnboardLanguageService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LangLevelTypeRepository langLevelTypeRepository;
    private final OnboardLangLevelRepository onboardLangLevelRepository;

    @Override
    public void saveNativeLanguage(UUID userId,NativeLanguageRequest req) {
        int nativeLangId = req.languageId();

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("USER NOT FOUND"));
        Language language = languageRepository.findById(nativeLangId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND LANGUAGE"));
        user.setNativeLanguage(language);
        userRepository.save(user);

    }

    @Override
    public void saveLanguageLevel(UUID userId,LanguageLevelRequest req) {
        Set<Integer> langLevelTypeIds = req.languages().stream()
                .map(LanguageLevelRequest.LanguageLevelDto::langLevelTypeId)
                .collect(Collectors.toSet());
        Map<Integer, LangLevelType> langLevelTypeMap = langLevelTypeRepository
                .findAllById(langLevelTypeIds)
                .stream()
                .collect(Collectors.toMap(LangLevelType::getLangLevelId, Function.identity()));

        for (LanguageLevelRequest.LanguageLevelDto dto : req.languages()) {
            OnboardLangLevelId id = new OnboardLangLevelId(userId, dto.languageId());
            LangLevelType langLevelType = langLevelTypeMap.get(dto.langLevelTypeId());
            if (langLevelType == null) {
                throw new NotFoundException("LANGUAGE LEVEL NOT FOUND: ");
            }
            OnboardLangLevel onboardLangLevel = OnboardLangLevel.builder()
                    .id(id)
                    .langLevelType(langLevelType)
                    .build();
            onboardLangLevelRepository.save(onboardLangLevel);
        }

    }


    @Override
    public List<LanguageResponse> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(lang -> new LanguageResponse(
                        lang.getLanguageId(),
                        lang.getLanguageName()
                ))
                .toList();
    }

    @Override
    public List<LangLevelTypeResponse> getLanguageLevelTypes() {
        return langLevelTypeRepository.findAll().stream()
                .filter(lang -> lang.getLangLevelId() >= 100 && lang.getLangLevelId() <200)
                .map(lang -> new LangLevelTypeResponse(lang.getLangLevelId(),lang.getLangLevelName()))
                .toList();
    }

    @Override
    public List<LangLevelTypeResponse> getPartnerLevelTypes() {
        return langLevelTypeRepository.findAll().stream()
                .filter(lang -> lang.getLangLevelId() >= 200 && lang.getLangLevelId() <300)
                .map(lang -> new LangLevelTypeResponse(lang.getLangLevelId(), lang.getLangLevelName()))
                .toList();
    }


}
