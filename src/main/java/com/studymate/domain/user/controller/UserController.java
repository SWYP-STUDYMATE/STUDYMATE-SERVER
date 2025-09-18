package com.studymate.domain.user.controller;

import com.studymate.common.dto.response.ApiResponse;
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
    public ApiResponse<Void> saveEnglishName (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody EnglishNameRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveEnglishName(userId,req);
        return ApiResponse.success("영어 이름이 저장되었습니다.");
    }

    @PostMapping("/birthyear")
    public ApiResponse<Void> saveBirthYear (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody BirthyearRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveBirthYear(userId,req);
        return ApiResponse.success("출생년도가 저장되었습니다.");
    }

    @PostMapping("/birthday")
    public ApiResponse<Void> saveBirthDay (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody BirthdayRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveBirthDay(userId,req);
        return ApiResponse.success("생일이 저장되었습니다.");
    }

    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    public ApiResponse<ProfileImageUrlResponse> saveProfileImage (@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestPart("file")MultipartFile file
                                  ) {
        UUID userId = principal.getUuid();
        
        // 디버깅을 위한 상세 로그 추가
        System.out.println("=== Profile Image Upload Debug ===");
        System.out.println("User ID: " + userId);
        System.out.println("File is null: " + (file == null));
        if (file != null) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            System.out.println("Is empty: " + file.isEmpty());
        }
        System.out.println("==================================");
        
        ProfileImageUrlResponse response = userService.saveProfileImage(userId,file);
        return ApiResponse.success(response, "프로필 이미지가 업로드되었습니다.");
    }

    @PostMapping("/gender")
    public ApiResponse<Void> saveUserGender (@AuthenticationPrincipal CustomUserDetails principal,
                            @RequestBody UserGenderTypeRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveUserGender(userId,req);
        return ApiResponse.success("성별이 저장되었습니다.");
    }

    @PostMapping("/self-bio")
    public ApiResponse<Void> saveSelfBio (@AuthenticationPrincipal CustomUserDetails principal,
                             @RequestBody SelfBioRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveSelfBio(userId,req);
        return ApiResponse.success("자기소개가 저장되었습니다.");
    }

    @PostMapping  ("/location")
    public ApiResponse<Void> saveLocation (@AuthenticationPrincipal CustomUserDetails principal,
                              @RequestBody LocationRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveLocation(userId,req);
        return ApiResponse.success("위치가 저장되었습니다.");
    }
    @GetMapping("/locations")
    public ApiResponse<List<LocationResponse>> getAllLocation() {
        List<LocationResponse> locations = userService.getAllLocation();
        return ApiResponse.success(locations);
    }

    @GetMapping("/name")
    public ApiResponse<UserNameResponse> getUserName(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        UserNameResponse response = userService.getUserName(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(@AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID userId = principal.getUuid();
        UserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/profile-image")
    public ApiResponse<ProfileImageUrlResponse> getProfileImageUrl(@AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID userId = principal.getUuid();
        ProfileImageUrlResponse response = userService.getProfileImageUrl(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/gender-type")
    public ApiResponse<List<UserGenderTypeResponse>> getAllUserGender(
    ) {
        List<UserGenderTypeResponse> genderTypes = userService.getAllUserGender();
        return ApiResponse.success(genderTypes);
    }

    // 프론트엔드 연동을 위한 추가 API 엔드포인트들
    
    @GetMapping("/complete-profile")
    public ApiResponse<UserCompleteProfileResponse> getCompleteProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        UserCompleteProfileResponse response = userService.getCompleteProfile(userId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/complete-profile")
    public ApiResponse<Void> updateCompleteProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody UserCompleteProfileRequest req) {
        UUID userId = principal.getUuid();
        userService.updateCompleteProfile(userId, req);
        return ApiResponse.success("프로필이 업데이트되었습니다.");
    }
    
    @GetMapping("/onboarding-status")
    public ApiResponse<OnboardingStatusResponse> getOnboardingStatus(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        OnboardingStatusResponse response = userService.getOnboardingStatus(userId);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/complete-onboarding")
    public ApiResponse<Void> completeOnboarding(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody CompleteOnboardingRequest req) {
        UUID userId = principal.getUuid();
        userService.completeOnboarding(userId, req);
        return ApiResponse.success("온보딩이 완료되었습니다.");
    }
    
    @GetMapping("/language-info")
    public ApiResponse<UserLanguageInfoResponse> getUserLanguageInfo(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        UserLanguageInfoResponse response = userService.getUserLanguageInfo(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/settings")
    public ApiResponse<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        UserSettingsResponse response = userService.getUserSettings(userId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/settings")
    public ApiResponse<Void> updateUserSettings(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody UserSettingsRequest req) {
        UUID userId = principal.getUuid();
        userService.updateUserSettings(userId, req);
        return ApiResponse.success("설정이 업데이트되었습니다.");
    }
}
