package com.studymate.domain.ai.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.ai.service.WorkersAIService;
import com.studymate.domain.leveltest.domain.dto.response.VoiceAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkersAIServiceImpl implements WorkersAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workers.api.url:https://workers.languagemate.kr}")
    private String workersApiUrl;

    @Value("${workers.internal.secret:studymate-internal-secret-2024}")
    private String workersInternalSecret;

    @Value("${scoring.mode:live}")
    private String scoringMode;



    @Override
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            String url = workersApiUrl + "/api/v1/internal/transcribe";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", workersInternalSecret);

            byte[] audioBytes = audioFile.getBytes();
            String audioBase64 = java.util.Base64.getEncoder().encodeToString(audioBytes);

            Map<String, Object> body = new HashMap<>();
            body.put("audio_base64", audioBase64);
            body.put("language", "auto");
            body.put("task", "transcribe");
            body.put("user_context", Map.of(
                    "filename", audioFile.getOriginalFilename() != null ? audioFile.getOriginalFilename() : "audio.wav",
                    "size", audioBytes.length,
                    "content_type", audioFile.getContentType() != null ? audioFile.getContentType() : "audio/wav"
            ));

            log.info("[WorkersAI] /transcribe req url={}, size={}", url, audioBytes.length);

            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            log.info("[WorkersAI] /transcribe resp status={}, body={}", resp.getStatusCode(), resp.getBody());

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                Map<String, Object> rb = resp.getBody();
                Map<String, Object> data = safeGetMap(rb, "data");
                if (data != null) return String.valueOf(data.getOrDefault("transcript", ""));
                return String.valueOf(rb.getOrDefault("transcript", ""));
            }
            return "";
        } catch (Exception e) {
            log.error("[WorkersAI] /transcribe error", e);
            return "";
        }
    }

    @Override
    public VoiceAnalysisResponse evaluateLevelTest(String transcript, String language, Map<String, Object> questions) {
        if (!"live".equalsIgnoreCase(scoringMode)) {
            log.warn("[WorkersAI] scoring.mode='{}'", scoringMode);
            return fixedStub(transcript);
        }
        try {
            String url = workersApiUrl + "/api/v1/internal/level-test";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", workersInternalSecret);

            Map<String, Object> body = new HashMap<>();
            body.put("transcript", transcript);
            body.put("language", language);
            body.put("questions", questions);

            log.info("[WorkersAI] /level-test req url={}, body={}", url, body);

            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            log.info("[WorkersAI] /level-test resp status={}, body={}", resp.getStatusCode(), resp.getBody());

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                Map<String, Object> rb = resp.getBody();
                Map<String, Object> data = safeGetMap(rb, "data");
                Map<String, Object> evaluation = null;
                if (data != null) evaluation = safeGetMap(data, "evaluation");
                if (evaluation == null) evaluation = safeGetMap(rb, "evaluation");

                if (evaluation == null) {
                    log.warn("[WorkersAI] evaluation missing. fallback.");
                    return createDefaultAnalysisResponse(transcript);
                }
                return buildVoiceAnalysisResponse(evaluation);
            }
            return createDefaultAnalysisResponse(transcript);
        } catch (Exception e) {
            log.error("[WorkersAI] /level-test error", e);
            return createDefaultAnalysisResponse(transcript);
        }
    }

    private VoiceAnalysisResponse fixedStub(String transcript) {
        VoiceAnalysisResponse.ScoreBreakdown sb = VoiceAnalysisResponse.ScoreBreakdown.builder()
                .pronunciationScore(70).fluencyScore(70).grammarScore(70).vocabularyScore(70).build();
        return VoiceAnalysisResponse.builder()
                .overallScore(70)
                .cefrLevel("B1")
                .scoreBreakdown(sb)
                .strengths("")
                .weaknesses("")
                .recommendations(Arrays.asList())
                .feedback("Stub scoring (demo).")
                .analyzedAt(LocalDateTime.now())
                .build();
    }


    @Override
    public Map<String, Object> generateRealtimeFeedback(String transcript, String context, String userLevel) {
        try {
            String url = workersApiUrl + "/api/v1/internal/conversation-feedback";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", workersInternalSecret);

            Map<String, Object> body = new HashMap<>();
            body.put("transcript", transcript);
            body.put("context", context);
            body.put("user_level", userLevel);
            body.put("user_context", Map.of("request_time", System.currentTimeMillis(), "source", "spring-boot-server"));

            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                Map<String, Object> rb = resp.getBody();
                Map<String, Object> data = safeGetMap(rb, "data");
                if (data != null) return safeGetMap(data, "feedback");
                return safeGetMap(rb, "feedback");
            }
            return defaultFeedback();
        } catch (Exception e) {
            log.error("[WorkersAI] /conversation-feedback error", e);
            return defaultFeedback();
        }
    }

    @Override
    public Map<String, Object> generateLearningRecommendations(String userLevel, Map<String, Object> weaknesses) {
        Map<String, Object> rec = new HashMap<>();
        List<String> contents = new ArrayList<>();
        List<String> exercises = new ArrayList<>();

        switch (userLevel) {
            case "A1":
            case "A2":
                contents.add("Basic vocabulary flashcards");
                contents.add("Simple conversation dialogues");
                exercises.add("Daily greeting practice");
                exercises.add("Basic sentence construction");
                break;
            case "B1":
            case "B2":
                contents.add("Intermediate reading passages");
                contents.add("Podcast episodes for learners");
                exercises.add("Conversation role-play scenarios");
                exercises.add("Grammar pattern drills");
                break;
            default:
                contents.add("Advanced literature excerpts");
                contents.add("Native speaker podcasts");
                exercises.add("Debate and discussion topics");
                exercises.add("Professional presentation practice");
        }
        rec.put("recommendedContents", contents);
        rec.put("practiceExercises", exercises);
        rec.put("estimatedTimePerDay", "30-45 minutes");
        rec.put("focusAreas", weaknesses != null ? weaknesses.keySet() : Collections.emptySet());
        return rec;
    }

    // -------- helpers --------
    @SuppressWarnings("unchecked")
    private Map<String, Object> safeGetMap(Map<String, Object> m, String k) {
        if (m == null) return null;
        Object v = m.get(k);
        return (v instanceof Map) ? (Map<String, Object>) v : null;
    }

    private int getIntValue(Map<String, Object> map, String key) {
        if (map == null) return 0;
        Object v = map.get(key);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) { return 0; }
    }


    @SuppressWarnings("unchecked")
    private VoiceAnalysisResponse buildVoiceAnalysisResponse(Map<String, Object> eval) {
        // 1) scores 맵 꺼내기
        Map<String, Object> scores = safeGetMap(eval, "scores");

        // 2) 점수: scores 우선, 없으면 eval 루트 폴백
        int pron  = getIntValue(scores != null ? scores : eval, "pronunciation");
        int flu   = getIntValue(scores != null ? scores : eval, "fluency");
        int gram  = getIntValue(scores != null ? scores : eval, "grammar");
        int vocab = getIntValue(scores != null ? scores : eval, "vocabulary");

        // 3) overallScore 계산(없으면 평균)
        int overall = getIntValue(eval, "overallScore");
        if (overall == 0) {
            int cnt = 0, sum = 0;
            for (int v : new int[]{pron, flu, gram, vocab}) {
                if (v > 0) { sum += v; cnt++; }
            }
            overall = (cnt > 0) ? Math.round(sum / (float) cnt) : 0;
        }

        // 4) level: estimatedLevel 우선 → cefrLevel 폴백
        String level = String.valueOf(
                eval.getOrDefault("estimatedLevel", eval.getOrDefault("cefrLevel", "B1"))
        );

        // 5) 추천/강점/약점
        List<String> recs = asStringList(eval.get("recommendations"));
        if (recs.isEmpty()) recs = asStringList(eval.get("suggestions"));
        String strengths = String.join(", ", asStringList(eval.get("strengths")));
        String weaknesses = String.join(", ", asStringList(eval.get("weaknesses")));

        // 6) scoreBreakdown 객체 생성
        VoiceAnalysisResponse.ScoreBreakdown sb = VoiceAnalysisResponse.ScoreBreakdown.builder()
                .pronunciationScore(pron)
                .fluencyScore(flu)
                .grammarScore(gram)
                .vocabularyScore(vocab)
                .build();

        // 7) 최종 DTO
        return VoiceAnalysisResponse.builder()
                .cefrLevel(level)
                .overallScore(overall)
                .scoreBreakdown(sb)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .recommendations(recs)
                .feedback(String.valueOf(eval.getOrDefault("feedback", "Keep practicing!")))
                .analyzedAt(LocalDateTime.now())
                .build();
    }


    private VoiceAnalysisResponse createDefaultAnalysisResponse(String transcript) {
        int wc = (transcript == null || transcript.isBlank()) ? 0 : transcript.trim().split("\\s+").length;
        int base = Math.min(50 + wc / 5, 85);
        VoiceAnalysisResponse.ScoreBreakdown sb = VoiceAnalysisResponse.ScoreBreakdown.builder()
                .pronunciationScore(base)
                .fluencyScore(base)
                .grammarScore(base)
                .vocabularyScore(base)
                .build();
        return VoiceAnalysisResponse.builder()
                .overallScore(base)
                .cefrLevel(base >= 80 ? "C1" : base >= 70 ? "B2" : base >= 60 ? "B1" : base >= 50 ? "A2" : "A1")
                .scoreBreakdown(sb)
                .strengths("Clear communication attempt")
                .weaknesses("Could improve fluency and vocabulary range")
                .recommendations(Arrays.asList("Practice speaking regularly", "Expand vocabulary", "Focus on pronunciation"))
                .feedback("Keep practicing to improve your English skills!")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    private List<String> asStringList(Object v) {
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        return new ArrayList<>();
    }

    private Map<String, Object> defaultFeedback() {
        Map<String, Object> m = new HashMap<>();
        m.put("corrections", new ArrayList<>());
        m.put("suggestions", Arrays.asList("Try to speak more clearly", "Use complete sentences"));
        m.put("encouragement", "Good effort! Keep practicing!");
        m.put("fluencyScore", 70);
        return m;
    }
}
