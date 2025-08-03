package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.ProfileImageUrlResponse;
import com.studymate.domain.user.domain.dto.response.UserGenderTypeResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void saveEnglishName(UUID userId,EnglishNameRequest req);
    void saveProfileImage(UUID userId, MultipartFile file);
    void saveSelfBio(UUID userId,SelfBioRequest req);
    void saveLocation(UUID userId,LocationRequest req);
    void saveUserGender(UUID userId, UserGenderTypeRequest req);
    void saveBirthYear (UUID userId, BirthyearRequest req);
    void saveBirthDay (UUID userId, BirthdayRequest req);
    List<LocationResponse> getAllLocation();
    UserNameResponse getUserName(UUID userId);
    ProfileImageUrlResponse getProfileImageUrl(UUID userId);
    List<UserGenderTypeResponse> getAllUserGender();

}
