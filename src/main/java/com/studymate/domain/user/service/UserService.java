package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.*;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void saveEnglishName(UUID userId,EnglishNameRequest req);
    ProfileImageUrlResponse saveProfileImage(UUID userId, MultipartFile file);
    void saveSelfBio(UUID userId,SelfBioRequest req);
    void saveLocation(UUID userId,LocationRequest req);
    void saveUserGender(UUID userId, UserGenderTypeRequest req);
    void saveBirthYear (UUID userId, BirthyearRequest req);
    void saveBirthDay (UUID userId, BirthdayRequest req);
    List<LocationResponse> getAllLocation();
    UserNameResponse getUserName(UUID userId);
    ProfileImageUrlResponse getProfileImageUrl(UUID userId);
    List<UserGenderTypeResponse> getAllUserGender();

    // 프론트엔드 연동을 위한 추가 메서드들
    UserCompleteProfileResponse getCompleteProfile(UUID userId);
    void updateCompleteProfile(UUID userId, UserCompleteProfileRequest req);
    OnboardingStatusResponse getOnboardingStatus(UUID userId);
    void completeOnboarding(UUID userId, CompleteOnboardingRequest req);
    UserSettingsResponse getUserSettings(UUID userId);
    void updateUserSettings(UUID userId, UserSettingsRequest req);
    UserLanguageInfoResponse getUserLanguageInfo(UUID userId);
    
    // 통합 프로필 조회
    UserProfileResponse getUserProfile(UUID userId);
    
    // V2 API 메소드들
    UserProfileResponseV2 getUserProfileV2(UUID userId);
    UserNameResponse updateEnglishName(UUID userId, EnglishNameRequest request);
    LocationResponseV2 updateLocationV2(UUID userId, LocationRequest request);
    BirthYearResponse updateBirthYear(UUID userId, BirthyearRequest request);
    UserGenderTypeResponse updateGenderV2(UUID userId, UserGenderTypeRequest request);
    void updateSelfBio(UUID userId, SelfBioRequest request);
    ProfileImageUrlResponse updateProfileImage(UUID userId, ProfileImageRequest request);
    List<LocationResponseV2> getAvailableLocationsV2();
    List<LanguageResponse> getAvailableLanguages();
    List<UserGenderTypeResponse> getGenderTypes();
}
