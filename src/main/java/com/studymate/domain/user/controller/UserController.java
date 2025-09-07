package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.*;
import com.studymate.domain.user.service.UserService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/english-name")
    public void saveEnglishName (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody EnglishNameRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveEnglishName(userId,req);
    }

    @PostMapping("/birthyear")
    public void saveBirthYear (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody BirthyearRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveBirthYear(userId,req);
    }

    @PostMapping("/birthday")
    public void saveBirthDay (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody BirthdayRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveBirthDay(userId,req);
    }

    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    public ProfileImageUrlResponse saveProfileImage (@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestPart("file")MultipartFile file
                                  ) {
        UUID userId = principal.getUuid();
        return userService.saveProfileImage(userId,file);
    }

    @PostMapping("/gender")
    public void saveUserGender (@AuthenticationPrincipal CustomUserDetails principal,
                            @RequestBody UserGenderTypeRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveUserGender(userId,req);
    }

    @PostMapping("/self-bio")
    public void saveSelfBio (@AuthenticationPrincipal CustomUserDetails principal,
                             @RequestBody SelfBioRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveSelfBio(userId,req);
    }

    @PostMapping  ("/location")
    public void saveLocation (@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestBody LocationRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveLocation(userId,req);
    }
    @GetMapping("/locations")
    public List<LocationResponse> getAllLocation() {
        return userService.getAllLocation();
    }

    @GetMapping("/name")
    public UserNameResponse getUserName(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return userService.getUserName(userId);
    }

    @GetMapping("/profile")
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID userId = principal.getUuid();
        return userService.getUserProfile(userId);
    }
    
    @GetMapping("/profile-image")
    public ProfileImageUrlResponse getProfileImageUrl(@AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID userId = principal.getUuid();
        return userService.getProfileImageUrl(userId);
    }

    @GetMapping("/gender-type")
    public List<UserGenderTypeResponse> getAllUserGender(
    ) {
        return userService.getAllUserGender();
    }

    // 프론트엔드 연동을 위한 추가 API 엔드포인트들
    
    @GetMapping("/complete-profile")
    public UserCompleteProfileResponse getCompleteProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return userService.getCompleteProfile(userId);
    }
    
    @PutMapping("/complete-profile")
    public void updateCompleteProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody UserCompleteProfileRequest req) {
        UUID userId = principal.getUuid();
        userService.updateCompleteProfile(userId, req);
    }
    
    @GetMapping("/onboarding-status")
    public OnboardingStatusResponse getOnboardingStatus(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return userService.getOnboardingStatus(userId);
    }
    
    @PostMapping("/complete-onboarding")
    public void completeOnboarding(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody CompleteOnboardingRequest req) {
        UUID userId = principal.getUuid();
        userService.completeOnboarding(userId, req);
    }
    
    @GetMapping("/settings")
    public UserSettingsResponse getUserSettings(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return userService.getUserSettings(userId);
    }
    
    @PutMapping("/settings")
    public void updateUserSettings(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody UserSettingsRequest req) {
        UUID userId = principal.getUuid();
        userService.updateUserSettings(userId, req);
    }
}
