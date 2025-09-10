package com.studymate.domain.user.controller;

import com.studymate.common.dto.ApiResponse;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;
import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.LocationResponseV2;
import com.studymate.domain.user.domain.dto.response.UserGenderTypeResponse;
import com.studymate.domain.user.domain.dto.response.UserProfileResponseV2;
import com.studymate.domain.user.service.UserService;
import com.studymate.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 사용자 관리 컨트롤러 V2 - 클라이언트 정합성 개선 버전
 * 
 * 모든 응답이 클라이언트 TypeScript 인터페이스와 완벽하게 일치
 * 
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
@Tag(name = "User V2", description = "사용자 관리 API v2 - 클라이언트 정합성 개선")
public class UserControllerV2 {
    
    private final UserService userService;
    
    /**
     * 사용자 프로필 조회
     * 
     * @return UserProfileResponseV2 (클라이언트 인터페이스와 일치)
     */
    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인한 사용자의 상세 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponseV2>> getUserProfile(
            @CurrentUser UUID userId) {
        
        UserProfileResponseV2 profile = userService.getUserProfileV2(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
    
    /**
     * 영어 이름 업데이트
     */
    @PutMapping("/english-name")
    @Operation(summary = "영어 이름 수정")
    public ResponseEntity<ApiResponse<UserNameResponse>> updateEnglishName(
            @CurrentUser UUID userId,
            @RequestBody EnglishNameRequest request) {
        
        UserNameResponse response = userService.updateEnglishName(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 위치 정보 업데이트
     * 
     * @return LocationResponseV2 (클라이언트 인터페이스와 일치)
     */
    @PutMapping("/location")
    @Operation(summary = "위치 정보 수정")
    public ResponseEntity<ApiResponse<LocationResponseV2>> updateLocation(
            @CurrentUser UUID userId,
            @RequestBody LocationRequest request) {
        
        LocationResponseV2 response = userService.updateLocationV2(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 생년 업데이트 - String을 Integer로 자동 변환
     */
    @PutMapping("/birth-year")
    @Operation(summary = "출생년도 수정")
    public ResponseEntity<ApiResponse<BirthYearResponse>> updateBirthYear(
            @CurrentUser UUID userId,
            @RequestBody BirthYearRequest request) {
        
        // request에서 Integer로 받아서 처리
        BirthYearResponse response = userService.updateBirthYear(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 성별 업데이트
     * 
     * @return UserGenderTypeResponse (클라이언트 인터페이스와 일치)
     */
    @PutMapping("/gender")
    @Operation(summary = "성별 정보 수정")
    public ResponseEntity<ApiResponse<UserGenderTypeResponse>> updateGender(
            @CurrentUser UUID userId,
            @RequestBody UserGenderTypeRequest request) {
        
        UserGenderTypeResponse response = userService.updateGenderV2(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 자기소개 업데이트
     */
    @PutMapping("/bio")
    @Operation(summary = "자기소개 수정")
    public ResponseEntity<ApiResponse<Void>> updateSelfBio(
            @CurrentUser UUID userId,
            @RequestBody SelfBioRequest request) {
        
        userService.updateSelfBio(userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 프로필 이미지 업데이트
     */
    @PutMapping("/profile-image")
    @Operation(summary = "프로필 이미지 수정")
    public ResponseEntity<ApiResponse<ProfileImageUrlResponse>> updateProfileImage(
            @CurrentUser UUID userId,
            @RequestBody ProfileImageRequest request) {
        
        ProfileImageUrlResponse response = userService.updateProfileImage(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 전체 프로필 업데이트 (일괄 수정)
     */
    @PutMapping("/profile/complete")
    @Operation(summary = "프로필 일괄 수정")
    public ResponseEntity<ApiResponse<UserCompleteProfileResponse>> updateCompleteProfile(
            @CurrentUser UUID userId,
            @RequestBody UserCompleteProfileRequest request) {
        
        UserCompleteProfileResponse response = userService.updateCompleteProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 사용 가능한 위치 목록 조회
     */
    @GetMapping("/locations")
    @Operation(summary = "위치 목록 조회", description = "선택 가능한 모든 위치 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<LocationResponseV2>>> getAvailableLocations() {
        
        List<LocationResponseV2> locations = userService.getAvailableLocationsV2();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }
    
    /**
     * 사용 가능한 언어 목록 조회
     */
    @GetMapping("/languages")
    @Operation(summary = "언어 목록 조회", description = "선택 가능한 모든 언어 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> getAvailableLanguages() {
        
        List<LanguageResponse> languages = userService.getAvailableLanguages();
        return ResponseEntity.ok(ApiResponse.success(languages));
    }
    
    /**
     * 성별 옵션 목록 조회
     */
    @GetMapping("/gender-types")
    @Operation(summary = "성별 옵션 조회", description = "선택 가능한 성별 옵션 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserGenderTypeResponse>>> getGenderTypes() {
        
        List<UserGenderTypeResponse> genderTypes = userService.getGenderTypes();
        return ResponseEntity.ok(ApiResponse.success(genderTypes));
    }
}

/**
 * 요청/응답 DTO 정의
 */
record UserNameResponse(
        String englishName,
        String userName
) {}

record ProfileImageUrlResponse(
        String imageUrl,
        String fileName
) {}

record BirthYearRequest(
        Integer birthYear  // Integer로 직접 받음
) {}

record BirthYearResponse(
        Integer birthYear  // Integer로 반환
) {}

record ProfileImageRequest(
        String imageUrl
) {}

record UserCompleteProfileResponse(
        String id,
        String englishName,
        String profileImageUrl,
        String selfBio,
        LocationResponseV2 location,
        LanguageResponse nativeLanguage,
        List<LanguageResponse> targetLanguages,
        Integer birthYear,  // Integer로 반환
        String birthday,
        UserGenderTypeResponse gender,
        String createdAt,
        String updatedAt,
        boolean onboardingCompleted,
        int profileCompleteness
) {}