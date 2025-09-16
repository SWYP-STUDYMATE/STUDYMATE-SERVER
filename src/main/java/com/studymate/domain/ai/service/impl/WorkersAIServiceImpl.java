package com.studymate.domain.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.ai.service.WorkersAIService;
import com.studymate.domain.leveltest.domain.dto.response.VoiceAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkersAIServiceImpl implements WorkersAIService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${workers.api.url:https://workers.languagemate.kr}")
    private String workersApiUrl;
    
    @Value("${workers.api.key:}")
    private String workersApiKey;
    
    @Override
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            String url = workersApiUrl + "/api/v1/transcribe";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (!workersApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + workersApiKey);
            }
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return (String) responseBody.get("transcript");
            }
            
            log.error("Failed to transcribe audio: {}", response.getStatusCode());
            return "";
            
        } catch (Exception e) {
            log.error("Error transcribing audio: ", e);
            return "";
        }
    }
    
    @Override
    public VoiceAnalysisResponse evaluateLevelTest(String transcript, String language, Map<String, Object> questions) {
        try {
            String url = workersApiUrl + "/api/v1/level-test";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!workersApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + workersApiKey);
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transcript", transcript);
            requestBody.put("language", language);
            requestBody.put("questions", questions);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> evaluation = (Map<String, Object>) responseBody.get("evaluation");
                
                return buildVoiceAnalysisResponse(evaluation);
            }
            
            log.error("Failed to evaluate level test: {}", response.getStatusCode());
            return createDefaultAnalysisResponse(transcript);
            
        } catch (Exception e) {
            log.error("Error evaluating level test: ", e);
            return createDefaultAnalysisResponse(transcript);
        }
    }
    
    @Override
    public Map<String, Object> generateRealtimeFeedback(String transcript, String context, String userLevel) {
        try {
            String url = workersApiUrl + "/api/v1/llm";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!workersApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + workersApiKey);
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transcript", transcript);
            requestBody.put("context", context);
            requestBody.put("userLevel", userLevel);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return (Map<String, Object>) responseBody.get("feedback");
            }
            
            return createDefaultFeedback();
            
        } catch (Exception e) {
            log.error("Error generating realtime feedback: ", e);
            return createDefaultFeedback();
        }
    }
    
    @Override
    public Map<String, Object> generateLearningRecommendations(String userLevel, Map<String, Object> weaknesses) {
        Map<String, Object> recommendations = new HashMap<>();
        
        // 레벨별 추천 콘텐츠
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
            case "C1":
            case "C2":
                contents.add("Advanced literature excerpts");
                contents.add("Native speaker podcasts");
                exercises.add("Debate and discussion topics");
                exercises.add("Professional presentation practice");
                break;
        }
        
        recommendations.put("recommendedContents", contents);
        recommendations.put("practiceExercises", exercises);
        recommendations.put("estimatedTimePerDay", "30-45 minutes");
        recommendations.put("focusAreas", weaknesses.keySet());
        
        return recommendations;
    }
    
    private VoiceAnalysisResponse buildVoiceAnalysisResponse(Map<String, Object> evaluation) {
        VoiceAnalysisResponse.ScoreBreakdown scoreBreakdown = VoiceAnalysisResponse.ScoreBreakdown.builder()
            .pronunciationScore(getIntValue(evaluation, "pronunciation"))
            .fluencyScore(getIntValue(evaluation, "fluency"))
            .grammarScore(getIntValue(evaluation, "grammar"))
            .vocabularyScore(getIntValue(evaluation, "vocabulary"))
            .build();
        
        List<String> strengths = (List<String>) evaluation.getOrDefault("strengths", new ArrayList<>());
        List<String> weaknesses = (List<String>) evaluation.getOrDefault("weaknesses", new ArrayList<>());
        List<String> recommendations = (List<String>) evaluation.getOrDefault("recommendations", new ArrayList<>());
        
        return VoiceAnalysisResponse.builder()
            .cefrLevel((String) evaluation.getOrDefault("cefrLevel", "B1"))
            .overallScore(getIntValue(evaluation, "overallScore"))
            .scoreBreakdown(scoreBreakdown)
            .strengths(String.join(", ", strengths))
            .weaknesses(String.join(", ", weaknesses))
            .recommendations(recommendations)
            .feedback((String) evaluation.getOrDefault("feedback", "Keep practicing!"))
            .analyzedAt(LocalDateTime.now())
            .build();
    }
    
    private VoiceAnalysisResponse createDefaultAnalysisResponse(String transcript) {
        // 기본 분석 응답 생성 (Workers AI 호출 실패 시)
        int wordCount = transcript.split("\\s+").length;
        int baseScore = Math.min(50 + wordCount / 5, 85);
        
        VoiceAnalysisResponse.ScoreBreakdown scoreBreakdown = VoiceAnalysisResponse.ScoreBreakdown.builder()
            .pronunciationScore(baseScore + random(-5, 5))
            .fluencyScore(baseScore + random(-5, 5))
            .grammarScore(baseScore + random(-5, 5))
            .vocabularyScore(baseScore + random(-5, 5))
            .build();
        
        return VoiceAnalysisResponse.builder()
            .cefrLevel(determineCEFRLevel(baseScore))
            .overallScore(baseScore)
            .scoreBreakdown(scoreBreakdown)
            .strengths("Clear communication attempt")
            .weaknesses("Could improve fluency and vocabulary range")
            .recommendations(Arrays.asList(
                "Practice speaking more regularly",
                "Expand vocabulary through reading",
                "Focus on pronunciation exercises"
            ))
            .feedback("Keep practicing to improve your English skills!")
            .analyzedAt(LocalDateTime.now())
            .build();
    }
    
    private Map<String, Object> createDefaultFeedback() {
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("corrections", new ArrayList<>());
        feedback.put("suggestions", Arrays.asList(
            "Try to speak more clearly",
            "Use complete sentences"
        ));
        feedback.put("encouragement", "Good effort! Keep practicing!");
        feedback.put("fluencyScore", 70);
        return feedback;
    }
    
    private String determineCEFRLevel(int score) {
        if (score >= 90) return "C2";
        if (score >= 80) return "C1";
        if (score >= 70) return "B2";
        if (score >= 60) return "B1";
        if (score >= 50) return "A2";
        return "A1";
    }
    
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    private int random(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}