package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.request.ProfileImageRequest;
import com.studymate.domain.user.domain.dto.request.SelfBioRequest;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
import com.studymate.domain.user.service.UserService;
import com.studymate.domain.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/english-name")
    public void saveEnglishName (@RequestBody EnglishNameRequest req) {
        userService.saveEnglishName(req);
    }

    @PostMapping("/profile-image")
    public void saveProfileImage (@RequestBody ProfileImageRequest req) {
        userService.saveProfileImage(req);
    }

    @PostMapping("/self-bio")
    public void saveSelfBio (@RequestBody SelfBioRequest req) {
        userService.saveSelfBio(req);
    }

    @PostMapping  ("/location")
    public void saveLocation (@RequestBody LocationRequest req) {
        userService.saveLocation(req);
    }
    @GetMapping("/locations")
    public List<LocationResponse> getAllLocation() {
        return userService.getAllLocation();
    }
    @GetMapping("/{userId}/name")
    public UserNameResponse getUserName(@PathVariable UUID userId) {
        return userService.getUserName(userId);
    }
}
