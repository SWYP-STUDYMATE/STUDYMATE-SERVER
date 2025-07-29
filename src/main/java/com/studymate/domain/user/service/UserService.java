package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.request.ProfileImageRequest;
import com.studymate.domain.user.domain.dto.request.SelfBioRequest;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void saveEnglishName(UUID userId,EnglishNameRequest req);
    void saveProfileImage(UUID userId,ProfileImageRequest req);
    void saveSelfBio(UUID userId,SelfBioRequest req);
    void saveLocation(UUID userId,LocationRequest req);
    List<LocationResponse> getAllLocation();
    UserNameResponse getUserName(UUID userId);

}
