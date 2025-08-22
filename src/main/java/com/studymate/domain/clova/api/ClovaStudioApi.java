package com.studymate.domain.clova.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.clova.domain.dto.response.CorrectionDetail;
import com.studymate.domain.clova.domain.dto.response.EnglishCorrectionResponse;
import com.studymate.domain.clova.domain.dto.type.CorrectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClovaStudioApi {
    private final ClovaStudioConfig config;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public EnglishCorrectionResponse correctEnglish(String originalText) {
        try {
            String prompt = buildPrompt(originalText);
            var request = new ClovaRequest(config.getModel(), prompt);

            String response = webClient.post()
                    .uri(config.getEndpoint())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(originalText, response);

        } catch (Exception e) {
            log.error("CLOVA Studio API 오류", e);
            return createErrorResponse(originalText, "영어 교정 처리 중 오류가 발생했습니다.");
        }
    }

    private String buildPrompt(String text) {
        return String.format("""
            다음 영어 문장을 교정해주세요: "%s"
            
            JSON 형식으로 응답해주세요:
            {
                "correctedText": "교정된 문장",
                "corrections": [
                    {
                        "original": "원본 부분",
                        "corrected": "수정된 부분",
                        "reason": "수정 이유",
                        "type": "GRAMMAR|SPELLING|PUNCTUATION|VOCABULARY|STYLE"
                    }
                ],
                "explanation": "전체적인 설명"
            }
            """, text);
    }

    private EnglishCorrectionResponse parseResponse(String originalText, String response) {
        try {
            JsonNode json = objectMapper.readTree(response);
            JsonNode choices = json.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                return parseContent(originalText, content);
            }
        } catch (Exception e) {
            log.error("응답 파싱 오류", e);
        }

        return createErrorResponse(originalText, "교정 처리 중 오류가 발생했습니다.");
    }

    private EnglishCorrectionResponse parseContent(String originalText, String content) {
        try {
            // JSON 부분 추출
            String jsonStr = extractJsonFromContent(content);
            if (jsonStr != null) {
                JsonNode json = objectMapper.readTree(jsonStr);
                return buildCorrectionResponse(originalText, json);
            }
        } catch (Exception e) {
            log.error("컨텐츠 파싱 오류", e);
        }

        return createErrorResponse(originalText, "응답 파싱에 실패했습니다.");
    }

    private String extractJsonFromContent(String content) {
        int start = content.indexOf("```json");
        int end = content.lastIndexOf("```");

        if (start != -1 && end != -1 && end > start) {
            return content.substring(start + 7, end).trim();
        }

        // ```json이 없는 경우 { }로 시작하는 JSON 찾기
        start = content.indexOf("{");
        end = content.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1).trim();
        }

        return null;
    }

    private EnglishCorrectionResponse buildCorrectionResponse(String originalText, JsonNode json) {
        List<CorrectionDetail> corrections = new ArrayList<>();
        JsonNode correctionsNode = json.get("corrections");

        if (correctionsNode != null && correctionsNode.isArray()) {
            for (JsonNode node : correctionsNode) {
                CorrectionType type = parseCorrectionType(node.get("type").asText());
                corrections.add(new CorrectionDetail(
                        node.get("original").asText(),
                        node.get("corrected").asText(),
                        node.get("reason").asText(),
                        type
                ));
            }
        }

        return new EnglishCorrectionResponse(
                originalText,
                json.get("correctedText").asText(),
                corrections,
                json.get("explanation").asText()

        );
    }

    private CorrectionType parseCorrectionType(String type) {
        try {
            return CorrectionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CorrectionType.GRAMMAR; // 기본값
        }
    }

    private EnglishCorrectionResponse createErrorResponse(String originalText, String errorMessage) {
        return new EnglishCorrectionResponse(
                originalText,
                originalText,
                new ArrayList<>(),
                errorMessage

        );
    }

    private static class ClovaRequest {
        public String model;
        public List<Message> messages;

        public ClovaRequest(String model, String content) {
            this.model = model;
            this.messages = List.of(new Message("user", content));
        }

        private static class Message {
            public String role;
            public String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }
    }
}
