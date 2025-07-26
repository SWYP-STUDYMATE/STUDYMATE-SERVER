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
    void saveEnglishName(EnglishNameRequest req);
    void saveProfileImage(ProfileImageRequest req);
    void saveSelfBio(SelfBioRequest req);
    void saveLocation(LocationRequest req);
    List<LocationResponse> getAllLocation();
    UserNameResponse getUserName(UUID userId);

}
