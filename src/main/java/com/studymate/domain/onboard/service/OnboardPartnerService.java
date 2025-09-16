package com.studymate.domain.onboard.service;

import com.studymate.domain.onboard.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboard.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboard.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboard.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboard.domain.type.PartnerGenderType;

import java.util.List;
import java.util.UUID;

public interface OnboardPartnerService {
    void savePartnerGender (UUID userId,PartnerGenderRequest req);
    void savePartnerPersonality (UUID userId,PartnerRequest req);
    List<PartnerGenderResponse> getAllPartnerGenderType();
    List<PartnerPersonalityResponse> getAllPartnerPersonality();
}
