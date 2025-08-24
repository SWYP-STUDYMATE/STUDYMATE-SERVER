package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboarding.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboarding.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
import com.studymate.domain.onboarding.service.OnboardPartnerService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.domain.user.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/onboard/partner")
public class OnboardPartnerController {

    private final OnboardPartnerService onboardPartnerService;
    private final JwtUtils jwtUtils;

    @PostMapping("/personality")
    public void savePartnerPersonality(@AuthenticationPrincipal CustomUserDetails principal,
                                       @RequestBody PartnerRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardPartnerService.savePartnerPersonality(userId,req);
    }

    @PostMapping("/gender")
    public void savePartnerGender(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody PartnerGenderRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardPartnerService.savePartnerGender(userId,req);
    }

    @GetMapping("/gender-type")
    public List<PartnerGenderResponse> getAllPartnerGenderType() {
        return onboardPartnerService.getAllPartnerGenderType();
    }

    @GetMapping("/personalities")
    public List<PartnerPersonalityResponse> getAllPartnerPersonality(){
        return onboardPartnerService.getAllPartnerPersonality();
    }



}
