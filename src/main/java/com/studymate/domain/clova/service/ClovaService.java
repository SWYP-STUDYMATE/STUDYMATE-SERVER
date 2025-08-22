package com.studymate.domain.clova.service;

import com.studymate.domain.clova.domain.dto.request.EnglishCorrectionRequest;
import com.studymate.domain.clova.domain.dto.response.EnglishCorrectionResponse;

public interface ClovaService {
    EnglishCorrectionResponse correctEnglish(EnglishCorrectionRequest req);
}
