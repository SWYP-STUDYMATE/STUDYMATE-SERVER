package com.studymate.domain.clova.domain.dto.response;

import com.studymate.domain.clova.domain.dto.type.CorrectionType;

public record CorrectionDetail(
        String original,
        String corrected,
        String reason,
        CorrectionType type
) {
}

