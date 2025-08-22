package com.studymate.domain.clova.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.clova.api.ClovaStudioApi;
import com.studymate.domain.clova.api.ClovaStudioConfig;
import com.studymate.domain.clova.domain.dto.request.EnglishCorrectionRequest;
import com.studymate.domain.clova.domain.dto.response.CorrectionDetail;
import com.studymate.domain.clova.domain.dto.response.EnglishCorrectionResponse;
import com.studymate.domain.clova.domain.dto.type.CorrectionType;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClovaServiceImpl implements ClovaService{
    private final RestTemplate restTemplate;

    @Value("${clova.studio.endpoint}")
    private String clovaApiUrl;

    @Value("${clova.studio.api-key}")
    private String clovaApiKey;

    @Override
    public EnglishCorrectionResponse correctEnglish(EnglishCorrectionRequest request) {
        try {
            // 1. 요청 헤더 세팅
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + clovaApiKey);
            headers.set("Content-Type", "application/json");

            // 2. 요청 바디(JSON)
            String body = """
                    {
                      "messages": [
                        {
                          "role": "system",
                          "content": "당신은 전문적인 영어 교정 도우미입니다. 반드시 JSON 형식으로만 응답하세요."
                        },
                        {
                          "role": "user",
                          "content": "{ \\"originalText\\": \\"%s\\" }"
                        }
                      ],
                      "model": "HCX-003"
                    }
                    """.formatted(request.originalText());

            // 3. POST 호출
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(clovaApiUrl, entity, String.class);

            // 4. 응답 파싱
            return parseCorrectionResponse(response.getBody(), request.originalText());

        } catch (Exception e) {
            return new EnglishCorrectionResponse(
                    request.originalText(),
                    request.originalText(),
                    List.of(),
                    "영어 교정 처리 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }


    private EnglishCorrectionResponse parseCorrectionResponse(String responseBody, String originalText) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(responseBody);

            // CLOVA가 role=assistant → content 안에 JSON을 준다고 가정
            String content = node.path("result").path("message").path("content").asText();

            JsonNode json = mapper.readTree(content);

            String correctedText = json.path("correctedText").asText(originalText);
            String explanation = json.path("explanation").asText("");

            List<CorrectionDetail> corrections = new ArrayList<>();
            if (json.has("corrections")) {
                for (JsonNode corr : json.get("corrections")) {
                    corrections.add(new CorrectionDetail(
                            corr.path("original").asText(),
                            corr.path("corrected").asText(),
                            corr.path("reason").asText(),
                            CorrectionType.valueOf(corr.path("type").asText().toUpperCase())
                    ));
                }
            }

            return new EnglishCorrectionResponse(originalText, correctedText, corrections, explanation);

        } catch (Exception e) {
            throw new RuntimeException("CLOVA 응답 파싱 실패", e);
        }
    }

}






