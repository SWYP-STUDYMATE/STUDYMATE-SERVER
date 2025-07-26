package com.studymate.domain.user.domain.dto.request;

import java.util.UUID;

public record LocationRequest(
        UUID userId,
        int locationId
) {
}
