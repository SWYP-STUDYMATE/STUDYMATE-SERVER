package com.studymate.domain.user.domain.dto.response;

public record LocationResponse(
        int locationId,
        String country,
        String city,
        String timezone
) {
}
