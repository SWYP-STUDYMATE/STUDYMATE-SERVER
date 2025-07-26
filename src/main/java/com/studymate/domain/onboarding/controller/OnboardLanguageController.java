package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.LanguageLevelRequest;
import com.studymate.domain.onboarding.domain.dto.request.NativeLanguageRequest;
import com.studymate.domain.onboarding.domain.dto.response.LangLevelTypeResponse;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;
import com.studymate.domain.onboarding.service.OnboardLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboard/language")
public class OnboardLanguageController {

    private final OnboardLanguageService onboardLanguageService;

    @PostMapping("/native-language")
    public void saveNativeLanguage(@RequestBody NativeLanguageRequest req){
        onboardLanguageService.saveNativeLanguage(req);
    }

    @PostMapping("/language-level")
    public void saveLanguageLevel(@RequestBody LanguageLevelRequest req){
        onboardLanguageService.saveLanguageLevel(req);
    }

//    @PostMapping("/learning-language-level")
//    public void saveLearningLanguageLevel(@RequestBody LanguageLevelRequest req) {
//        onboardLanguageService.saveLearningLanguageLevel(req);
//    }

    @GetMapping("/languages")
    public List<LanguageResponse> getAllLanguages() {
        return onboardLanguageService.getAllLanguages();
    }

    //언어레벨만
    @GetMapping("/level-types-language")
    public List<LangLevelTypeResponse> getLanguageLevelTypes(){
        return onboardLanguageService.getLanguageLevelTypes();
    }
    //파트너 레벨만
    @GetMapping("/level-types-partner")
    public List<LangLevelTypeResponse> getPartnerLevelTypes() {
        return onboardLanguageService.getPartnerLevelTypes();
    }


}
