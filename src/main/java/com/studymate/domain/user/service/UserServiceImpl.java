package com.studymate.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.*;
import com.studymate.domain.user.domain.repository.LocationRepository;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.domain.type.UserGenderType;
import com.studymate.domain.user.entity.Location;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.onboarding.domain.repository.OnboardingTopicRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingPartnerRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingScheduleRepository;
import com.studymate.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final AmazonS3Client amazonS3;
    private final OnboardingTopicRepository onboardingTopicRepository;
    private final OnboardingPartnerRepository onboardingPartnerRepository;
    private final OnboardingScheduleRepository onboardingScheduleRepository;

    @Value("${cloud.ncp.storage.bucket-name}")
    private String bucketName;

    @Override
    public void saveEnglishName(UUID userId,EnglishNameRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setEnglishName(req.englishName());
        userRepository.save(user);
    }

    @Override
    public ProfileImageUrlResponse saveProfileImage(UUID userId, MultipartFile file){
        log.info("프로필 이미지 업로드 시작 - 사용자: {}", userId);
        
        // 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            log.error("파일이 null이거나 비어있음 - 사용자: {}, file: {}", userId, file);
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        log.info("파일 정보 - 이름: {}, 크기: {}, 타입: {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        // 파일 크기 검증 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            log.error("파일 크기 초과 - 사용자: {}, 크기: {}MB", userId, file.getSize() / (1024 * 1024));
            throw new IllegalArgumentException("파일 크기가 10MB를 초과합니다.");
        }
        
        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("잘못된 파일 타입 - 사용자: {}, 타입: {}", userId, contentType);
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));

        // 파일명 처리 (카메라 촬영 시 null이 될 수 있음)
        String originalFilename = file.getOriginalFilename();
        String filename = (originalFilename != null && !originalFilename.isEmpty()) 
            ? originalFilename 
            : "profile_" + System.currentTimeMillis() + ".jpg";
            
        String key = "profile-image/" + userId + "_" + filename;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);//공개 설정
            amazonS3.putObject(request);

            String url = amazonS3.getUrl(bucketName, key).toString();
            user.setProfileImage(url);
            userRepository.save(user);
            
            log.info("프로필 이미지 업로드 성공 - 사용자: {}, URL: {}", userId, url);
            
            return new ProfileImageUrlResponse(url);
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패 - 사용자: {}, 파일: {}, 오류: {}", 
                     userId, filename, e.getMessage());
            throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }
    @Override
     public void saveUserGender(UUID userId, UserGenderTypeRequest req) {
        UserGenderType userGenderType = req.genderType();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("User NOT FOUND"));
        user.setUserGenderType(userGenderType);
        userRepository.save(user);
    }

    @Override
    public void saveSelfBio(UUID userId,SelfBioRequest req){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setSelfBio(req.selfBio());
        userRepository.save(user);
    }

    @Override
    public void saveLocation(UUID userId,LocationRequest req) {
        int locationId = req.locationId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
       Location location = locationRepository.findById(locationId)
                       .orElseThrow(()-> new NotFoundException("NOT FOUND LOCATION"));
        user.setLocation(location);
        userRepository.save(user);

    }
    @Override
    public void saveBirthYear (UUID userId, BirthyearRequest req){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setBirthyear(req.birthyear());
        userRepository.save(user);

    }
    @Override
    public void saveBirthDay (UUID userId, BirthdayRequest req){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setBirthday(req.birthday());
        userRepository.save(user);

    }

    @Override
    public List<LocationResponse> getAllLocation() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(loc -> new LocationResponse(
                        loc.getLocationId(),
                        loc.getCountry(),
                        loc.getCity(),
                        loc.getTimeZone()
                ))
                .toList();

    }

    @Override
    public UserNameResponse getUserName(UUID userId) {
        String name = userRepository.findNameByUserId(userId);
        return new UserNameResponse(name);
    }

    @Override
    public ProfileImageUrlResponse getProfileImageUrl(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        return new ProfileImageUrlResponse(user.getProfileImage());

    }
    @Override
    public List<UserGenderTypeResponse> getAllUserGender() {
        return Arrays.stream(UserGenderType.values())
                .map(e-> new UserGenderTypeResponse(e.name(),e.getDescription()))
                .toList();
    }

    // 프론트엔드 연동을 위한 추가 메서드 구현들
    
    @Override
    public UserCompleteProfileResponse getCompleteProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        return new UserCompleteProfileResponse(
                user.getEnglishName(),
                user.getName(),
                user.getProfileImage(),
                user.getSelfBio(),
                user.getEmail(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getBirthyear() != null ? Integer.parseInt(user.getBirthyear()) : null,
                user.getBirthday(),
                user.getLocation() != null ? user.getLocation().getCity() : null,
                user.getNativeLanguage() != null ? user.getNativeLanguage().getName() : null,
                null, // targetLanguage - 온보딩 데이터에서 가져와야 함
                null, // languageLevel - 온보딩 데이터에서 가져와야 함
                user.getIsOnboardingCompleted()
        );
    }
    
    @Override
    public void updateCompleteProfile(UUID userId, UserCompleteProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        if (req.getEnglishName() != null) {
            user.setEnglishName(req.getEnglishName());
        }
        if (req.getKoreanName() != null) {
            user.setName(req.getKoreanName());
        }
        if (req.getSelfBio() != null) {
            user.setSelfBio(req.getSelfBio());
        }
        if (req.getGender() != null) {
            user.setGender(UserGenderType.valueOf(req.getGender()));
        }
        if (req.getBirthYear() != null) {
            user.setBirthyear(req.getBirthYear());
        }
        if (req.getBirthday() != null) {
            user.setBirthday(req.getBirthday());
        }
        if (req.getLocationId() != null) {
            Location location = locationRepository.findById(req.getLocationId())
                    .orElseThrow(() -> new NotFoundException("NOT FOUND LOCATION"));
            user.setLocation(location);
        }
        
        userRepository.save(user);
    }
    
    @Override
    public OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        // 기본 정보 완성도 체크
        boolean basicInfoCompleted = user.getEnglishName() != null && 
                                   user.getBirthyear() != null && 
                                   user.getGender() != null;
        
        // 온보딩 단계별 완성도 체크 - 실제 온보딩 관련 테이블들 확인
        boolean languageInfoCompleted = user.getNativeLanguage() != null;
        boolean interestInfoCompleted = !onboardingTopicRepository.findByUsrId(user.getUserId()).isEmpty();
        boolean partnerInfoCompleted = !onboardingPartnerRepository.findByUsrId(user.getUserId()).isEmpty();
        boolean scheduleInfoCompleted = !onboardingScheduleRepository.findByUsrId(user.getUserId()).isEmpty();
        
        int completedSteps = 0;
        if (basicInfoCompleted) completedSteps++;
        if (languageInfoCompleted) completedSteps++;
        if (interestInfoCompleted) completedSteps++;
        if (partnerInfoCompleted) completedSteps++;
        if (scheduleInfoCompleted) completedSteps++;
        
        return new OnboardingStatusResponse(
                basicInfoCompleted,
                languageInfoCompleted,
                interestInfoCompleted,
                partnerInfoCompleted,
                scheduleInfoCompleted,
                user.getIsOnboardingCompleted(),
                completedSteps,
                5
        );
    }
    
    @Override
    public void completeOnboarding(UUID userId, CompleteOnboardingRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        // 온보딩 데이터를 각각의 테이블에 저장
        // 1. 언어 정보 저장 - OnboardingLangLevel 엔티티 생성 필요시 구현
        log.info("Completing onboarding for user: {} with data: {}", userId, req);
        // 2. 관심사 정보 저장 (OnboardingMotivation, OnboardingTopic 등)
        // 3. 파트너 선호도 저장 (OnboardingPartner 등)
        // 4. 스케줄 정보 저장 (OnboardingSchedule 등)
        
        user.setIsOnboardingCompleted(true);
        userRepository.save(user);
    }
    
    @Override
    public UserSettingsResponse getUserSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        // 기본값으로 설정 (실제로는 UserSettings 테이블이 필요)
        return new UserSettingsResponse(
                true,   // emailNotifications
                true,   // pushNotifications
                false,  // smsNotifications
                true,   // profilePublic (inverted from privateProfile)
                true,   // showOnlineStatus
                true,   // allowMessages
                "ko",   // language
                user.getLocation() != null ? user.getLocation().getTimeZone() : "Asia/Seoul", // timezone
                "auto"  // theme
        );
    }
    
    @Override
    public void updateUserSettings(UUID userId, UserSettingsRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        // UserSettings 엔티티 생성 후 실제 설정 저장 - 향후 UserSettings 엔티티 구현 예정
        log.info("Updating user settings for user: {}", userId);
        if (req.getNotifications() != null) {
            log.info("Notification settings - email: {}, push: {}, sms: {}", 
                    req.getNotifications().getEmail(), 
                    req.getNotifications().getPush(), 
                    req.getNotifications().getSms());
        }
        if (req.getPrivacy() != null) {
            log.info("Privacy settings - profilePublic: {}, showOnlineStatus: {}, allowMessages: {}", 
                    req.getPrivacy().getProfilePublic(), 
                    req.getPrivacy().getShowOnlineStatus(), 
                    req.getPrivacy().getAllowMessages());
        }
        if (req.getPreferences() != null) {
            log.info("Preference settings - language: {}, timezone: {}, theme: {}", 
                    req.getPreferences().getLanguage(), 
                    req.getPreferences().getTimezone(), 
                    req.getPreferences().getTheme());
        }
        
        userRepository.save(user);
    }
    
    @Override
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        LocationResponse locationResponse = null;
        if (user.getLocation() != null) {
            Location location = user.getLocation();
            locationResponse = new LocationResponse(
                location.getLocationId(),
                location.getCountry(),
                location.getCity(),
                location.getTimeZone()
            );
        }
        
        // TODO: Add proper nativeLanguage and targetLanguages from user onboarding data
        // For now, using null values - this should be implemented properly
        return new UserProfileResponse(
                user.getUserId().toString(),                    // String id
                user.getEnglishName(),                          // String englishName  
                user.getProfileImage(),                         // String profileImageUrl
                user.getSelfBio(),                              // String selfBio
                locationResponse,                               // LocationResponse location
                null,                                           // LanguageResponse nativeLanguage - TODO: fetch from onboarding
                null,                                           // List<LanguageResponse> targetLanguages - TODO: fetch from onboarding
                user.getBirthyear() != null ? Integer.parseInt(user.getBirthyear()) : null, // Integer birthYear
                user.getBirthday(),                             // String birthday
                new UserGenderTypeResponse(
                                         user.getUserGenderType().name(), 
                                         user.getUserGenderType().getDescription()), // UserGenderTypeResponse gender
                user.getUserCreatedAt(),                        // LocalDateTime createdAt  
                user.getUserCreatedAt()                         // LocalDateTime updatedAt - TODO: add proper updatedAt field to User entity
        );
    }
    
    // V2 API 메소드들 구현
    @Override
    public UserProfileResponseV2 getUserProfileV2(UUID userId) {
        // 기존 getUserProfile과 동일한 로직을 사용하되 V2 Response 형식으로 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        // TODO: 실제 V2 응답 형식에 맞게 구현 필요
        // 임시로 null 반환
        return null;
    }
    
    @Override
    public UserNameResponse updateEnglishName(UUID userId, EnglishNameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        user.setEnglishName(request.englishName());
        userRepository.save(user);
        
        return new UserNameResponse(user.getEnglishName());
    }
    
    @Override
    public LocationResponseV2 updateLocationV2(UUID userId, LocationRequest request) {
        // TODO: 실제 구현 필요
        return null;
    }
    
    @Override
    public BirthYearResponse updateBirthYear(UUID userId, BirthyearRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        user.setBirthyear(request.birthyear());
        userRepository.save(user);
        
        return new BirthYearResponse(Integer.parseInt(request.birthyear()));
    }
    
    @Override
    public UserGenderTypeResponse updateGenderV2(UUID userId, UserGenderTypeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        user.setUserGenderType(request.genderType());
        userRepository.save(user);
        
        return UserGenderTypeResponse.from(user.getUserGenderType());
    }
    
    @Override
    public void updateSelfBio(UUID userId, SelfBioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        user.setSelfBio(request.selfBio());
        userRepository.save(user);
    }
    
    @Override
    public ProfileImageUrlResponse updateProfileImage(UUID userId, ProfileImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        user.setProfileImage(request.profileImage());
        userRepository.save(user);
        
        return new ProfileImageUrlResponse(user.getProfileImage());
    }
    
    
    @Override
    public List<LocationResponseV2> getAvailableLocationsV2() {
        // TODO: 실제 구현 필요
        return List.of();
    }
    
    @Override
    public List<com.studymate.domain.onboarding.domain.dto.response.LanguageResponse> getAvailableLanguages() {
        // TODO: 실제 구현 필요
        return List.of();
    }
    
    @Override
    public List<UserGenderTypeResponse> getGenderTypes() {
        return Arrays.stream(UserGenderType.values())
                .map(UserGenderTypeResponse::from)
                .toList();
    }
    
    private String extractFileName(String url) {
        if (url == null || url.isEmpty()) return null;
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
