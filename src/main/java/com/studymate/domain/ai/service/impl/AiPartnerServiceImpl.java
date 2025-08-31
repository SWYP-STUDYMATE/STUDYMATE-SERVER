package com.studymate.domain.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.ai.domain.dto.request.SendMessageRequest;
import com.studymate.domain.ai.domain.dto.request.StartAiSessionRequest;
import com.studymate.domain.ai.domain.dto.response.AiMessageResponse;
import com.studymate.domain.ai.domain.dto.response.AiPartnerResponse;
import com.studymate.domain.ai.domain.dto.response.AiSessionResponse;
import com.studymate.domain.ai.entity.AiMessage;
import com.studymate.domain.ai.entity.AiPartner;
import com.studymate.domain.ai.entity.AiSession;
import com.studymate.domain.ai.repository.AiMessageRepository;
import com.studymate.domain.ai.repository.AiPartnerRepository;
import com.studymate.domain.ai.repository.AiSessionRepository;
import com.studymate.domain.ai.service.AiPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiPartnerServiceImpl implements AiPartnerService {
    
    private final AiPartnerRepository aiPartnerRepository;
    private final AiSessionRepository aiSessionRepository;
    private final AiMessageRepository aiMessageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SESSION_CACHE_PREFIX = "ai_session:";
    private static final String USER_PREFERENCE_PREFIX = "user_ai_pref:";
    private static final String PARTNER_STATS_PREFIX = "ai_partner_stats:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);
    
    @Override
    @Transactional(readOnly = true)
    public List<AiPartnerResponse> getAvailablePartners(String targetLanguage, String languageLevel) {
        List<AiPartner> partners;
        
        if (targetLanguage != null && languageLevel != null) {
            partners = aiPartnerRepository.findByLanguageAndLevel(targetLanguage, languageLevel);
        } else {
            partners = aiPartnerRepository.findAllActive();
        }
        
        return partners.stream()
                .map(this::buildPartnerResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AiPartnerResponse> getRecommendedPartners(UUID userId) {
        List<AiPartner> topRated = aiPartnerRepository.findTopRatedPartners();
        
        return topRated.stream()
                .limit(5)
                .map(partner -> {
                    AiPartnerResponse response = buildPartnerResponse(partner);
                    response.setIsRecommended(true);
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AiPartnerResponse getPartnerDetails(UUID partnerId) {
        AiPartner partner = aiPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        return buildPartnerResponse(partner);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AiPartnerResponse> searchPartners(String keyword, String specialty, String personalityType) {
        List<AiPartner> partners = new ArrayList<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            partners.addAll(aiPartnerRepository.searchByKeyword(keyword));
        }
        
        if (specialty != null && !specialty.isEmpty()) {
            partners.addAll(aiPartnerRepository.findBySpecialty(specialty));
        }
        
        if (personalityType != null && !personalityType.isEmpty()) {
            partners.addAll(aiPartnerRepository.findByPersonalityType(personalityType));
        }
        
        if (partners.isEmpty()) {
            partners = aiPartnerRepository.findAllActive();
        }
        
        return partners.stream()
                .distinct()
                .map(this::buildPartnerResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public AiSessionResponse startSession(UUID userId, StartAiSessionRequest request) {
        Optional<AiSession> activeSession = aiSessionRepository.findActiveSessionByUserId(userId);
        if (activeSession.isPresent()) {
            throw new RuntimeException("이미 진행 중인 AI 세션이 있습니다");
        }
        
        AiPartner partner = aiPartnerRepository.findById(request.getAiPartnerId())
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        AiSession session = AiSession.builder()
                .userId(userId)
                .aiPartnerId(request.getAiPartnerId())
                .sessionTitle(request.getSessionTitle())
                .sessionType(request.getSessionType())
                .status(AiSession.SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .messageCount(0)
                .learningObjectives(request.getLearningObjectives())
                .build();
        
        session = aiSessionRepository.save(session);
        
        AiMessage greetingMessage = createGreetingMessage(session, partner);
        aiMessageRepository.save(greetingMessage);
        
        updatePartnerSessionCount(partner.getId());
        cacheActiveSession(userId, session);
        
        log.info("AI session started: {} with partner: {} for user: {}", 
            session.getId(), partner.getName(), userId);
        
        return buildSessionResponse(session, partner);
    }
    
    @Override
    @Async
    public CompletableFuture<AiMessageResponse> sendMessage(UUID userId, SendMessageRequest request) {
        AiSession session = aiSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        if (session.getStatus() != AiSession.SessionStatus.ACTIVE) {
            throw new RuntimeException("활성 상태가 아닌 세션입니다");
        }
        
        long startTime = System.currentTimeMillis();
        
        AiMessage userMessage = AiMessage.builder()
                .sessionId(session.getId())
                .senderType(AiMessage.SenderType.USER)
                .messageContent(request.getMessageContent())
                .messageType(AiMessage.MessageType.TEXT)
                .build();
        
        userMessage = aiMessageRepository.save(userMessage);
        
        try {
            AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                    .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
            
            String aiResponse = generateAiResponse(partner, session, request.getMessageContent());
            
            AiMessage aiMessage = AiMessage.builder()
                    .sessionId(session.getId())
                    .senderType(AiMessage.SenderType.AI_PARTNER)
                    .messageContent(aiResponse)
                    .messageType(AiMessage.MessageType.TEXT)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .tokensUsed(calculateTokensUsed(request.getMessageContent(), aiResponse))
                    .confidenceScore(0.95)
                    .build();
            
            if (request.getRequestCorrection()) {
                processTextCorrection(userMessage, aiMessage, request.getMessageContent());
            }
            
            if (request.getRequestFeedback()) {
                generateFeedback(aiMessage, request.getMessageContent());
            }
            
            aiMessage = aiMessageRepository.save(aiMessage);
            
            updateSessionMessageCount(session);
            
            return CompletableFuture.completedFuture(buildMessageResponse(aiMessage));
            
        } catch (Exception e) {
            log.error("Error generating AI response for session {}: {}", session.getId(), e.getMessage());
            throw new RuntimeException("AI 응답 생성 중 오류가 발생했습니다");
        }
    }
    
    @Override
    public AiSessionResponse endSession(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        if (session.getStatus() != AiSession.SessionStatus.ACTIVE) {
            throw new RuntimeException("활성 상태가 아닌 세션입니다");
        }
        
        session.setStatus(AiSession.SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        
        if (session.getStartedAt() != null) {
            long durationMinutes = Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes();
            session.setDurationMinutes((int) durationMinutes);
        }
        
        generateSessionSummary(sessionId);
        generateImprovementSuggestions(userId, sessionId);
        
        session = aiSessionRepository.save(session);
        clearActiveSessionCache(userId);
        
        AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        log.info("AI session ended: {} for user: {}", sessionId, userId);
        
        return buildSessionResponse(session, partner);
    }
    
    @Override
    public AiSessionResponse pauseSession(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        session.setStatus(AiSession.SessionStatus.PAUSED);
        session = aiSessionRepository.save(session);
        
        AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        return buildSessionResponse(session, partner);
    }
    
    @Override
    public AiSessionResponse resumeSession(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        session.setStatus(AiSession.SessionStatus.ACTIVE);
        session = aiSessionRepository.save(session);
        
        cacheActiveSession(userId, session);
        
        AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        return buildSessionResponse(session, partner);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AiSessionResponse getActiveSession(UUID userId) {
        Optional<AiSession> activeSession = aiSessionRepository.findActiveSessionByUserId(userId);
        
        if (activeSession.isEmpty()) {
            return null;
        }
        
        AiSession session = activeSession.get();
        AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                .orElseThrow(() -> new RuntimeException("AI 파트너를 찾을 수 없습니다"));
        
        return buildSessionResponse(session, partner);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AiSessionResponse> getUserSessions(UUID userId, Pageable pageable) {
        Page<AiSession> sessions = aiSessionRepository.findByUserId(userId, pageable);
        
        return sessions.map(session -> {
            AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                    .orElse(null);
            return buildSessionResponse(session, partner);
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AiMessageResponse> getSessionMessages(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        List<AiMessage> messages = aiMessageRepository.findBySessionIdOrderByCreatedAt(sessionId);
        
        return messages.stream()
                .map(this::buildMessageResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void rateSession(UUID userId, UUID sessionId, Integer rating, String feedback) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        session.setUserRating(rating);
        session.setUserFeedback(feedback);
        aiSessionRepository.save(session);
        
        updatePartnerRating(session.getAiPartnerId());
        
        log.info("Session {} rated {} stars by user {}", sessionId, rating, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AiSessionResponse.LearningProgress getLearningProgress(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        List<AiMessage> corrections = aiMessageRepository.findCorrectionsForSession(sessionId);
        List<AiMessage> userMessages = aiMessageRepository.findBySessionIdAndSenderType(
                sessionId, AiMessage.SenderType.USER);
        
        double accuracyScore = calculateAccuracyScore(corrections.size(), userMessages.size());
        int correctionCount = corrections.size();
        int vocabularyCount = extractVocabularyCount(sessionId);
        double engagementScore = calculateEngagementScore(session);
        
        return AiSessionResponse.LearningProgress.builder()
                .accuracyScore(accuracyScore)
                .correctionCount(correctionCount)
                .vocabularyCount(vocabularyCount)
                .engagementScore(engagementScore)
                .overallFeedback(generateOverallFeedback(accuracyScore, engagementScore))
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AiSessionResponse> getSessionsByType(UUID userId, AiSession.SessionType sessionType) {
        List<AiSession> sessions = aiSessionRepository.findByUserIdAndSessionType(userId, sessionType);
        
        return sessions.stream()
                .map(session -> {
                    AiPartner partner = aiPartnerRepository.findById(session.getAiPartnerId())
                            .orElse(null);
                    return buildSessionResponse(session, partner);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Async
    public CompletableFuture<String> generateSessionSummary(UUID sessionId) {
        try {
            List<AiMessage> messages = aiMessageRepository.findBySessionIdOrderByCreatedAt(sessionId);
            String summary = "세션에서 " + messages.size() + "개의 메시지를 주고받았습니다.";
            
            AiSession session = aiSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
            
            session.setSessionSummary(summary);
            aiSessionRepository.save(session);
            
            return CompletableFuture.completedFuture(summary);
        } catch (Exception e) {
            log.error("Error generating session summary for session {}: {}", sessionId, e.getMessage());
            return CompletableFuture.completedFuture("요약 생성 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Async
    public CompletableFuture<List<String>> generateImprovementSuggestions(UUID userId, UUID sessionId) {
        try {
            List<String> suggestions = Arrays.asList(
                "문법 정확도를 높이기 위해 더 많은 연습을 해보세요",
                "새로운 어휘를 활용한 문장 만들기를 시도해보세요",
                "발음 연습을 통해 자신감을 키워보세요"
            );
            
            AiSession session = aiSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
            
            session.setImprovementSuggestions(String.join("; ", suggestions));
            aiSessionRepository.save(session);
            
            return CompletableFuture.completedFuture(suggestions);
        } catch (Exception e) {
            log.error("Error generating improvement suggestions for session {}: {}", sessionId, e.getMessage());
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getLearnedVocabulary(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        if (session.getVocabularyLearned() != null && !session.getVocabularyLearned().isEmpty()) {
            return Arrays.asList(session.getVocabularyLearned().split(","));
        }
        
        return Collections.emptyList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getGrammarPoints(UUID userId, UUID sessionId) {
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다"));
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }
        
        if (session.getGrammarPoints() != null && !session.getGrammarPoints().isEmpty()) {
            return Arrays.asList(session.getGrammarPoints().split(","));
        }
        
        return Collections.emptyList();
    }
    
    private AiPartnerResponse buildPartnerResponse(AiPartner partner) {
        return AiPartnerResponse.builder()
                .id(partner.getId())
                .name(partner.getName())
                .description(partner.getDescription())
                .targetLanguage(partner.getTargetLanguage())
                .languageLevel(partner.getLanguageLevel())
                .personalityType(partner.getPersonalityType())
                .specialty(partner.getSpecialty())
                .avatarImage(partner.getAvatarImage())
                .voiceType(partner.getVoiceType())
                .aiModel(partner.getAiModel())
                .greetingMessage(partner.getGreetingMessage())
                .ratingAverage(partner.getRatingAverage())
                .ratingCount(partner.getRatingCount())
                .sessionCount(partner.getSessionCount())
                .isActive(partner.getIsActive())
                .isRecommended(false)
                .build();
    }
    
    private AiSessionResponse buildSessionResponse(AiSession session, AiPartner partner) {
        List<AiMessage> recentMessages = aiMessageRepository.findBySessionIdOrderByCreatedAt(session.getId())
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        
        List<AiMessageResponse> messageResponses = recentMessages.stream()
                .map(this::buildMessageResponse)
                .collect(Collectors.toList());
        
        List<String> vocabularyLearned = session.getVocabularyLearned() != null ? 
                Arrays.asList(session.getVocabularyLearned().split(",")) : 
                Collections.emptyList();
        
        List<String> grammarPoints = session.getGrammarPoints() != null ? 
                Arrays.asList(session.getGrammarPoints().split(",")) : 
                Collections.emptyList();
        
        return AiSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .aiPartnerId(session.getAiPartnerId())
                .aiPartnerName(partner != null ? partner.getName() : "Unknown")
                .aiPartnerAvatar(partner != null ? partner.getAvatarImage() : null)
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationMinutes(session.getDurationMinutes())
                .messageCount(session.getMessageCount())
                .userRating(session.getUserRating())
                .userFeedback(session.getUserFeedback())
                .learningObjectives(session.getLearningObjectives())
                .sessionSummary(session.getSessionSummary())
                .improvementSuggestions(session.getImprovementSuggestions())
                .vocabularyLearned(vocabularyLearned)
                .grammarPoints(grammarPoints)
                .recentMessages(messageResponses)
                .build();
    }
    
    private AiMessageResponse buildMessageResponse(AiMessage message) {
        List<AiMessageResponse.TextCorrection> corrections = new ArrayList<>();
        
        if (message.getCorrectionsJson() != null && !message.getCorrectionsJson().isEmpty()) {
            try {
                corrections = objectMapper.readValue(
                    message.getCorrectionsJson(), 
                    objectMapper.getTypeFactory().constructCollectionType(
                        List.class, AiMessageResponse.TextCorrection.class)
                );
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse corrections JSON: {}", e.getMessage());
            }
        }
        
        List<String> vocabularySuggestions = new ArrayList<>();
        if (message.getVocabularySuggestions() != null) {
            vocabularySuggestions = Arrays.asList(message.getVocabularySuggestions().split(","));
        }
        
        return AiMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderType(message.getSenderType())
                .messageContent(message.getMessageContent())
                .originalText(message.getOriginalText())
                .correctedText(message.getCorrectedText())
                .corrections(corrections)
                .grammarFeedback(message.getGrammarFeedback())
                .vocabularySuggestions(vocabularySuggestions)
                .pronunciationFeedback(message.getPronunciationFeedback())
                .confidenceScore(message.getConfidenceScore())
                .responseTimeMs(message.getResponseTimeMs())
                .messageType(message.getMessageType())
                .timestamp(message.getCreatedAt())
                .build();
    }
    
    private AiMessage createGreetingMessage(AiSession session, AiPartner partner) {
        String greetingText = partner.getGreetingMessage() != null ? 
                partner.getGreetingMessage() : 
                "안녕하세요! " + partner.getName() + "입니다. 오늘 함께 언어 연습을 해보세요!";
        
        return AiMessage.builder()
                .sessionId(session.getId())
                .senderType(AiMessage.SenderType.AI_PARTNER)
                .messageContent(greetingText)
                .messageType(AiMessage.MessageType.GREETING)
                .responseTimeMs(0L)
                .confidenceScore(1.0)
                .build();
    }
    
    private String generateAiResponse(AiPartner partner, AiSession session, String userMessage) {
        String responseTemplate = "그건 정말 흥미로운 말이네요! '%s'에 대해 더 자세히 설명해 주실 수 있나요?";
        return String.format(responseTemplate, userMessage.substring(0, Math.min(userMessage.length(), 20)));
    }
    
    private void processTextCorrection(AiMessage userMessage, AiMessage aiMessage, String originalText) {
        if (originalText.toLowerCase().contains("i are")) {
            List<AiMessageResponse.TextCorrection> corrections = Arrays.asList(
                AiMessageResponse.TextCorrection.builder()
                    .original("I are")
                    .corrected("I am")
                    .explanation("주语가 'I'일 때는 'am'을 사용합니다")
                    .correctionType("grammar")
                    .position(0)
                    .build()
            );
            
            try {
                aiMessage.setCorrectionsJson(objectMapper.writeValueAsString(corrections));
                aiMessage.setCorrectedText(originalText.replace("I are", "I am"));
                aiMessage.setGrammarFeedback("be 동사 사용법을 복습해보세요");
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize corrections: {}", e.getMessage());
            }
        }
        
        userMessage.setOriginalText(originalText);
    }
    
    private void generateFeedback(AiMessage aiMessage, String messageContent) {
        if (messageContent.length() > 50) {
            aiMessage.setVocabularySuggestions("excellent,wonderful,fantastic");
            aiMessage.setPronunciationFeedback("발음이 매우 좋아요!");
        }
    }
    
    private int calculateTokensUsed(String userMessage, String aiResponse) {
        return (userMessage.length() + aiResponse.length()) / 4;
    }
    
    private void updateSessionMessageCount(AiSession session) {
        int messageCount = aiMessageRepository.countMessagesBySessionId(session.getId());
        session.setMessageCount(messageCount);
        aiSessionRepository.save(session);
    }
    
    private void updatePartnerSessionCount(UUID partnerId) {
        AiPartner partner = aiPartnerRepository.findById(partnerId).orElse(null);
        if (partner != null) {
            partner.setSessionCount(partner.getSessionCount() != null ? partner.getSessionCount() + 1 : 1);
            aiPartnerRepository.save(partner);
        }
    }
    
    private void updatePartnerRating(UUID partnerId) {
        Double averageRating = aiSessionRepository.getAverageRatingByAiPartnerId(partnerId);
        int ratingCount = aiSessionRepository.getRatingCountByAiPartnerId(partnerId);
        
        AiPartner partner = aiPartnerRepository.findById(partnerId).orElse(null);
        if (partner != null) {
            partner.setRatingAverage(averageRating != null ? averageRating : 0.0);
            partner.setRatingCount(ratingCount);
            aiPartnerRepository.save(partner);
        }
    }
    
    private void cacheActiveSession(UUID userId, AiSession session) {
        try {
            String cacheKey = SESSION_CACHE_PREFIX + userId;
            String jsonSession = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(cacheKey, jsonSession, CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache active session: {}", e.getMessage());
        }
    }
    
    private void clearActiveSessionCache(UUID userId) {
        String cacheKey = SESSION_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
    
    private double calculateAccuracyScore(int correctionCount, int totalMessages) {
        if (totalMessages == 0) return 100.0;
        return Math.max(0, 100.0 - (correctionCount * 100.0 / totalMessages));
    }
    
    private int extractVocabularyCount(UUID sessionId) {
        List<AiMessage> messages = aiMessageRepository.findBySessionIdAndMessageType(
                sessionId, AiMessage.MessageType.SUGGESTION);
        return messages.size() * 2;
    }
    
    private double calculateEngagementScore(AiSession session) {
        if (session.getMessageCount() == null || session.getDurationMinutes() == null) {
            return 50.0;
        }
        
        double messagesPerMinute = session.getMessageCount().doubleValue() / 
                Math.max(1, session.getDurationMinutes().doubleValue());
        
        return Math.min(100.0, messagesPerMinute * 20);
    }
    
    private String generateOverallFeedback(double accuracyScore, double engagementScore) {
        if (accuracyScore >= 90 && engagementScore >= 80) {
            return "훌륭한 학습 세션이었습니다! 정확도와 참여도 모두 우수합니다.";
        } else if (accuracyScore >= 70) {
            return "좋은 진전을 보이고 있습니다. 계속 연습하면 더 향상될 것입니다.";
        } else {
            return "더 많은 연습이 필요합니다. 기본기를 다시 한 번 점검해보세요.";
        }
    }
}