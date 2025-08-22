package com.studymate.domain.clova.domain.dto.request;

import com.studymate.domain.clova.domain.dto.response.CorrectionDetail;
import lombok.Builder;

import java.util.List;
@Builder
public record EnglishCorrectionRequest(
        String originalText,
        String userId
) {
}
