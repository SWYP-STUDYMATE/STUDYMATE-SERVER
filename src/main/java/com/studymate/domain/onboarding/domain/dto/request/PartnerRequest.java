package com.studymate.domain.onboarding.domain.dto.request;

import java.util.List;
import java.util.UUID;

public record PartnerRequest(
        UUID userId,
        List<Integer> personalPartnerIds
) {
}
