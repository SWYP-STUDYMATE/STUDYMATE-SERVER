package com.studymate.domain.onboard.domain.dto.request;

import java.util.List;
import java.util.UUID;

public record PartnerRequest(
        List<Integer> personalPartnerIds
) {
}
