package com.studymate.domain.clova.domain.dto.response;

import java.util.List;

public record EnglishCorrectionResponse (
        String originalText,
        String correctedText,
        List<CorrectionDetail> corrections,
        String explanation
){
}
