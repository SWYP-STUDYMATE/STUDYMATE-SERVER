package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.request.*;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.ProfileImageUrlResponse;
import com.studymate.domain.user.domain.dto.response.UserGenderTypeResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
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
@RequestMapping("/user")
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
    public void saveProfileImage (@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestPart("file")MultipartFile file
                                  ) {
        UUID userId = principal.getUuid();
        userService.saveProfileImage(userId,file);
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
}
