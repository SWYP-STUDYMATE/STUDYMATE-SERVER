package com.studymate.domain.onboarding.domain.dto.request;

import com.studymate.domain.onboarding.domain.type.PartnerGenderType;

import java.util.UUID;

public record PartnerGenderRequest (
        UUID userId,
        PartnerGenderType partnerGenderType
){

}
