package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboarding.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboarding.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;

import java.util.List;
import java.util.UUID;

public interface OnboardingPartnerService {
    void savePartnerGender (UUID userId,PartnerGenderRequest req);
    void savePartnerPersonality (UUID userId,PartnerRequest req);
    List<PartnerGenderResponse> getAllPartnerGenderType();
    List<PartnerPersonalityResponse> getAllPartnerPersonality();
}
