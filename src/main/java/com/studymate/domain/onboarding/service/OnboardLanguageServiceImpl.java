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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardLanguageServiceImpl implements OnboardLanguageService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LangLevelTypeRepository langLevelTypeRepository;
    private final OnboardLangLevelRepository onboardLangLevelRepository;

    @Override
    public void saveNativeLanguage(UUID userId, NativeLanguageRequest req) {
        System.out.println("🔍 saveNativeLanguage 호출됨");
        System.out.println("🔍 userId: " + userId);
        System.out.println("🔍 req: " + req);
        
        int nativeLangId = req.languageId();
        System.out.println("🔍 nativeLangId: " + nativeLangId);
        
        if (nativeLangId <= 0) {
            System.out.println("🔍 Invalid language ID detected: " + nativeLangId);
            throw new IllegalArgumentException("Invalid language ID: " + nativeLangId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        Language language = languageRepository.findById(nativeLangId)
                .orElseThrow(() -> new NotFoundException("언어를 찾을 수 없습니다. ID: " + nativeLangId));
        
        System.out.println("🔍 언어 찾기 성공: " + language.getLanguageName());
        
        user.setNativeLanguage(language);
        userRepository.save(user);
        
        System.out.println("🔍 모국어 저장 완료");
    }

    @Override
    public void saveLanguageLevel(UUID userId,LanguageLevelRequest req) {
        // 현재 레벨과 목표 레벨 모두 수집
        Set<Integer> allLevelIds = req.languages().stream()
                .flatMap(dto -> Stream.of(dto.currentLevelId(), dto.targetLevelId()))
                .collect(Collectors.toSet());
                
        Map<Integer, LangLevelType> langLevelTypeMap = langLevelTypeRepository
                .findAllById(allLevelIds)
                .stream()
                .collect(Collectors.toMap(LangLevelType::getLangLevelId, Function.identity()));

        for (LanguageLevelRequest.LanguageLevelDto dto : req.languages()) {
            OnboardLangLevelId id = new OnboardLangLevelId(userId, dto.languageId());
            
            LangLevelType currentLevelType = langLevelTypeMap.get(dto.currentLevelId());
            LangLevelType targetLevelType = langLevelTypeMap.get(dto.targetLevelId());
            
            if (currentLevelType == null) {
                throw new NotFoundException("현재 언어 레벨을 찾을 수 없습니다: " + dto.currentLevelId());
            }
            if (targetLevelType == null) {
                throw new NotFoundException("목표 언어 레벨을 찾을 수 없습니다: " + dto.targetLevelId());
            }
            
            OnboardLangLevel onboardLangLevel = OnboardLangLevel.builder()
                    .id(id)
                    .langLevelType(currentLevelType) // 기존 호환성 유지
                    .currentLevel(currentLevelType)
                    .targetLevel(targetLevelType)
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
