package com.studymate.domain.onboard.controller;

import com.studymate.domain.onboard.domain.dto.request.LanguageLevelRequest;
import com.studymate.domain.onboard.domain.dto.request.NativeLanguageRequest;
import com.studymate.domain.onboard.domain.dto.response.LangLevelTypeResponse;
import com.studymate.domain.onboard.domain.dto.response.LanguageResponse;
import com.studymate.domain.onboard.service.OnboardLanguageService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.auth.jwt.JwtUtils;
import com.studymate.common.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/onboard/language")
public class OnboardLanguageController {

    private final OnboardLanguageService onboardLanguageService;
    private final JwtUtils jwtUtils;

    @PostMapping("/native-language")
    public ResponseEntity<ApiResponse<Void>> saveNativeLanguage(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody NativeLanguageRequest req) {
        
        System.out.println("🔍 saveNativeLanguage Controller 호출됨");
        System.out.println("🔍 Request body: " + req);
        
        UUID userId = principal.getUuid();
        System.out.println("🔍 User ID: " + userId);
        
        onboardLanguageService.saveNativeLanguage(userId, req);
        
        return ResponseEntity.ok(ApiResponse.success("모국어가 성공적으로 저장되었습니다."));
    }

    @PostMapping("/language-level")
    public ResponseEntity<ApiResponse<Void>> saveLanguageLevel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody LanguageLevelRequest req) {
        
        System.out.println("🔍 saveLanguageLevel Controller 호출됨");
        System.out.println("🔍 Request body: " + req);
        
        UUID userId = principal.getUuid();
        System.out.println("🔍 User ID: " + userId);
        
        onboardLanguageService.saveLanguageLevel(userId, req);
        
        return ResponseEntity.ok(ApiResponse.success("언어 레벨이 성공적으로 저장되었습니다."));
    }


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
