package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.*;
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
    
    // 통합 프로필 조회
    UserProfileResponse getUserProfile(UUID userId);
}
