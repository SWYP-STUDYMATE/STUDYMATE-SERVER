package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboarding.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboarding.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;

import java.util.List;

public interface OnboardPartnerService {
    void savePartnerGender (PartnerGenderRequest req);
    void savePartnerPersonality (PartnerRequest req);
    List<PartnerGenderResponse> getAllPartnerGenderType();
    List<PartnerPersonalityResponse> getAllPartnerPersonality();
}
