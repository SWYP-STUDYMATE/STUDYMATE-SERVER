package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.request.ProfileImageRequest;
import com.studymate.domain.user.domain.dto.request.SelfBioRequest;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
import com.studymate.domain.user.service.UserService;
import com.studymate.domain.user.service.UserServiceImpl;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/profile-image")
    public void saveProfileImage (@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody ProfileImageRequest req
    ) {
        UUID userId = principal.getUuid();
        userService.saveProfileImage(userId,req);
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
}
