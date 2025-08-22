package com.studymate.domain.clova.controller;

import com.studymate.common.dto.ResponseDto;
import com.studymate.domain.clova.domain.dto.request.EnglishCorrectionRequest;
import com.studymate.domain.clova.domain.dto.response.EnglishCorrectionResponse;
import com.studymate.domain.clova.service.ClovaService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clova")
public class ClovaController {
    private final ClovaService clovaService;

    @PostMapping("/correct-english")
    public ResponseEntity<ResponseDto<EnglishCorrectionResponse>> correctEnglish(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EnglishCorrectionRequest request
    ) {

        EnglishCorrectionRequest requestWithUser = EnglishCorrectionRequest.builder()
                .originalText(request.originalText())
                .userId(userDetails.getUsername())
                .build();

        EnglishCorrectionResponse response = clovaService.correctEnglish(requestWithUser);
        return ResponseEntity.ok(ResponseDto.of(response, "영어 교정이 완료되었습니다."));
    }

}
