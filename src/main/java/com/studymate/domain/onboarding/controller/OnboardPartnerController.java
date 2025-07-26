package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboarding.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboarding.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
import com.studymate.domain.onboarding.service.OnboardPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboard/partner")
public class OnboardPartnerController {

    private final OnboardPartnerService onboardPartnerService;

    @PostMapping("/personality")
    public void savePartnerPersonality(@RequestBody PartnerRequest req) {
        onboardPartnerService.savePartnerPersonality(req);
    }

    @PostMapping("/gender")
    public void savePartnerGender(@RequestBody PartnerGenderRequest req) {
        onboardPartnerService.savePartnerGender(req);
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
