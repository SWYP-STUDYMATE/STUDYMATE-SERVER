package com.studymate.domain.matching.service;

import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.matching.domain.dto.response.*;
import com.studymate.domain.matching.domain.repository.*;
import com.studymate.domain.matching.repository.MatchingRequestRepository;
import com.studymate.domain.matching.repository.UserMatchRepository;
import com.studymate.domain.matching.entity.*;
import com.studymate.domain.session.domain.repository.SessionRepository;
import com.studymate.domain.session.type.SessionStatus;
import com.studymate.domain.onboarding.domain.repository.MotivationRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingLangLevelRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingLearningExpectationRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingLearningStyleRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingMotivationRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingPartnerRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingTopicRepository;
import com.studymate.domain.onboarding.domain.repository.PartnerPersonalityRepository;
import com.studymate.domain.onboarding.domain.repository.TopicRepository;
import com.studymate.domain.onboarding.domain.repository.LearningStyleRepository;
import com.studymate.domain.onboarding.domain.repository.LearningExpectationRepository;
import com.studymate.domain.onboarding.entity.OnboardingLangLevel;
import com.studymate.domain.onboarding.entity.OnboardingLearningExpectation;
import com.studymate.domain.onboarding.entity.OnboardingLearningStyle;
import com.studymate.domain.onboarding.entity.OnboardingMotivation;
import com.studymate.domain.onboarding.entity.OnboardingPartner;
import com.studymate.domain.onboarding.entity.OnboardingTopic;
import com.studymate.domain.onboarding.entity.PartnerPersonality;
import com.studymate.domain.onboarding.entity.Topic;
import com.studymate.domain.onboarding.entity.Motivation;
import com.studymate.domain.onboarding.entity.LearningStyle;
import com.studymate.domain.onboarding.entity.LearningExpectation;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.service.UserStatusService;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchingServiceImpl implements MatchingService {

    private final UserRepository userRepository;
    private final MatchingRequestRepository matchingRequestRepository;
    private final UserMatchRepository userMatchRepository;
    private final CompatibilityCalculatorService compatibilityCalculatorService;
    private final UserStatusService userStatusService;
    private final MatchingQueueRepository matchingQueueRepository;
    private final MatchingFeedbackRepository matchingFeedbackRepository;
    private final SessionRepository sessionRepository;
    private final OnboardingLangLevelRepository onboardingLangLevelRepository;
    private final OnboardingMotivationRepository onboardingMotivationRepository;
    private final OnboardingTopicRepository onboardingTopicRepository;
    private final OnboardingLearningStyleRepository onboardingLearningStyleRepository;
    private final OnboardingLearningExpectationRepository onboardingLearningExpectationRepository;
    private final OnboardingPartnerRepository onboardingPartnerRepository;
    private final MotivationRepository motivationRepository;
    private final TopicRepository topicRepository;
    private final LearningStyleRepository learningStyleRepository;
    private final LearningExpectationRepository learningExpectationRepository;
    private final PartnerPersonalityRepository partnerPersonalityRepository;

    @Override
    public Page<RecommendedPartnerResponse> getRecommendedPartners(UUID userId, Pageable pageable,
                                                                 String nativeLanguage, String targetLanguage,
                                                                 String languageLevel, Integer minAge, Integer maxAge) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 기본적으로 온보딩이 완료된 사용자들을 대상으로 검색
        List<User> potentialPartners = userRepository.findPotentialPartners(userId);
        
        // 필터링 조건 적용
        List<RecommendedPartnerResponse> recommendedPartners = potentialPartners.stream()
                .filter(partner -> applyFilters(partner, nativeLanguage, targetLanguage, languageLevel, minAge, maxAge))
                .map(partner -> convertToRecommendedPartnerResponse(currentUser, partner))
                .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore())) // 호환성 점수 내림차순
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), recommendedPartners.size());
        
        if (start >= recommendedPartners.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, recommendedPartners.size());
        }
        
        return new PageImpl<>(recommendedPartners.subList(start, end), pageable, recommendedPartners.size());
    }

    @Override
    public Page<RecommendedPartnerResponse> getRecommendedPartnersAdvanced(UUID userId, 
                                                                          AdvancedMatchingFilterRequest filters, 
                                                                          Pageable pageable) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 고급 필터를 사용한 최적화된 쿼리
        Page<User> potentialPartners = userRepository.findPotentialPartnersWithFilters(userId, filters, pageable);
        
        List<RecommendedPartnerResponse> recommendedPartners = potentialPartners.getContent().stream()
                .map(partner -> convertToRecommendedPartnerResponse(currentUser, partner))
                .filter(response -> {
                    // 호환성 점수 필터 적용 (서비스 레벨에서)
                    if (filters.getMinCompatibilityScore() != null) {
                        return response.getCompatibilityScore() >= filters.getMinCompatibilityScore();
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    // 정렬 로직
                    String sortBy = filters.getSortBy() != null ? filters.getSortBy() : "compatibility";
                    boolean desc = !"asc".equals(filters.getSortDirection());
                    
                    return switch (sortBy.toLowerCase()) {
                        case "compatibility" -> desc ? 
                                Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()) :
                                Double.compare(a.getCompatibilityScore(), b.getCompatibilityScore());
                        default -> 0;
                    };
                })
                .collect(Collectors.toList());

        return new PageImpl<>(recommendedPartners, pageable, potentialPartners.getTotalElements());
    }

    @Override
    public Page<RecommendedPartnerResponse> getOnlinePartners(UUID userId, 
                                                             AdvancedMatchingFilterRequest filters, 
                                                             Pageable pageable) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 온라인 사용자만 조회
        Page<User> onlinePartners = userRepository.findOnlinePartners(userId, filters, pageable);
        
        List<RecommendedPartnerResponse> recommendedPartners = onlinePartners.getContent().stream()
                .map(partner -> convertToRecommendedPartnerResponse(currentUser, partner))
                .collect(Collectors.toList());

        return new PageImpl<>(recommendedPartners, pageable, onlinePartners.getTotalElements());
    }

    @Override
    public void sendMatchingRequest(UUID senderId, UUID targetUserId, String message) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND SENDER"));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND TARGET USER"));

        // 중복 요청 확인
        boolean existingRequest = matchingRequestRepository.existsBySenderAndReceiverAndStatus(
                sender, target, MatchingStatus.PENDING);
        
        if (existingRequest) {
            throw new IllegalStateException("이미 매칭 요청을 보낸 상대입니다.");
        }

        // 이미 매칭된 상대인지 확인 - ActiveMatchBetweenUsers 메서드 사용
        boolean alreadyMatched = userMatchRepository.findActiveMatchBetweenUsers(sender, target).isPresent();
        
        if (alreadyMatched) {
            throw new IllegalStateException("이미 매칭된 상대입니다.");
        }

        MatchingRequest matchingRequest = MatchingRequest.builder()
                .sender(sender)
                .receiver(target)
                .message(message)
                .status(MatchingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7일 후 만료
                .build();

        matchingRequestRepository.save(matchingRequest);
    }

    @Override
    public Page<SentMatchingRequestResponse> getSentMatchingRequests(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        List<MatchingRequest> requests = matchingRequestRepository.findBySenderOrderByCreatedAtDesc(user);
        // Pageable을 사용한 수동 페이징 (Repository에서 Page를 직접 지원하지 않는 경우)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), requests.size());
        List<MatchingRequest> pageContent = requests.subList(start, end);
        Page<MatchingRequest> pagedRequests = new PageImpl<>(pageContent, pageable, requests.size());
        
        return pagedRequests.map(this::convertToSentMatchingRequestResponse);
    }

    @Override
    public Page<ReceivedMatchingRequestResponse> getReceivedMatchingRequests(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        List<MatchingRequest> requests = matchingRequestRepository.findByReceiverOrderByCreatedAtDesc(user);
        // 수동 페이징
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), requests.size());
        List<MatchingRequest> pageContent = requests.subList(start, end);
        Page<MatchingRequest> pagedRequests = new PageImpl<>(pageContent, pageable, requests.size());
        
        return pagedRequests.map(request -> convertToReceivedMatchingRequestResponse(user, request));
    }

    @Override
    public void acceptMatchingRequest(UUID userId, UUID requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        MatchingRequest request = matchingRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND MATCHING REQUEST"));

        if (!request.getReceiver().getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청을 수락할 권한이 없습니다.");
        }

        if (request.getStatus() != MatchingStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        // 요청 상태 업데이트
        request.setStatus(MatchingStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        matchingRequestRepository.save(request);

        // 매칭 생성
        UserMatch userMatch = UserMatch.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .matchedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        userMatchRepository.save(userMatch);

        // 관련된 대기열 항목들을 MATCHED 상태로 업데이트
        List<MatchingQueue> senderQueue = matchingQueueRepository.findByUserOrderByJoinedAtDesc(request.getSender())
                .stream()
                .filter(queue -> MatchingQueue.QueueStatus.WAITING.equals(queue.getStatus()))
                .toList();
        List<MatchingQueue> receiverQueue = matchingQueueRepository.findByUserOrderByJoinedAtDesc(request.getReceiver())
                .stream()
                .filter(queue -> MatchingQueue.QueueStatus.WAITING.equals(queue.getStatus()))
                .toList();
        
        senderQueue.forEach(queue -> queue.updateStatus(MatchingQueue.QueueStatus.MATCHED));
        receiverQueue.forEach(queue -> queue.updateStatus(MatchingQueue.QueueStatus.MATCHED));
        
        matchingQueueRepository.saveAll(senderQueue);
        matchingQueueRepository.saveAll(receiverQueue);
    }

    @Override
    public void rejectMatchingRequest(UUID userId, UUID requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        MatchingRequest request = matchingRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND MATCHING REQUEST"));

        if (!request.getReceiver().getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청을 거절할 권한이 없습니다.");
        }

        if (request.getStatus() != MatchingStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        request.setStatus(MatchingStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        matchingRequestRepository.save(request);
    }

    @Override
    public Page<MatchedPartnerResponse> getMatchedPartners(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        List<UserMatch> matches = userMatchRepository.findActiveMatchesByUser(user);
        // 수동 페이징
        int start = (int) pageable.getOffset();

        if (start >= matches.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, matches.size());
        }

        int end = Math.min((start + pageable.getPageSize()), matches.size());
        List<UserMatch> pageContent = matches.subList(start, end);
        Page<UserMatch> pagedMatches = new PageImpl<>(pageContent, pageable, matches.size());
        
        return pagedMatches.map(match -> convertToMatchedPartnerResponse(user, match));
    }

    @Override
    public void removeMatch(UUID userId, UUID matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        UserMatch match = userMatchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND MATCH"));

        if (!match.getUser1().getUserId().equals(userId) && !match.getUser2().getUserId().equals(userId)) {
            throw new IllegalArgumentException("매칭을 삭제할 권한이 없습니다.");
        }

        match.deactivate(user); // UserMatch 엔티티의 deactivate 메서드 사용
        userMatchRepository.save(match);
    }

    @Override
    public CompatibilityScoreResponse getCompatibilityScore(UUID userId, UUID targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND TARGET USER"));

        return compatibilityCalculatorService.calculateCompatibility(user, target);
    }

    // Private helper methods

    private boolean applyFilters(User partner, String nativeLanguage, String targetLanguage,
                               String languageLevel, Integer minAge, Integer maxAge) {
        // 나이 필터
        if (minAge != null || maxAge != null) {
            Integer age = calculateAge(partner.getBirthyear());
            if (age != null) {
                if (minAge != null && age < minAge) return false;
                if (maxAge != null && age > maxAge) return false;
            }
        }

        // 언어 필터들은 실제 온보딩 데이터를 확인해야 함
        // 현재는 기본 구현만 제공
        
        return true;
    }

    private Integer calculateAge(String birthYear) {
        if (birthYear == null || birthYear.trim().isEmpty()) return null;
        try {
            return LocalDateTime.now().getYear() - Integer.parseInt(birthYear);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private RecommendedPartnerResponse convertToRecommendedPartnerResponse(User currentUser, User partner) {
        RecommendedPartnerResponse response = new RecommendedPartnerResponse();
        response.setUserId(partner.getUserId());
        response.setEnglishName(partner.getEnglishName());
        response.setProfileImageUrl(partner.getProfileImage());
        response.setSelfBio(partner.getSelfBio());
        response.setAge(calculateAge(partner.getBirthyear()));
        response.setGender(partner.getGender() != null ? partner.getGender().name() : null);
        response.setLocation(partner.getLocation() != null ? partner.getLocation().getCity() : null);
        response.setNativeLanguage(partner.getNativeLanguage() != null ? partner.getNativeLanguage().getName() : null);
        response.setTargetLanguages(getTargetLanguagesFromOnboarding(partner.getUserId()));
        response.setInterests(getInterestsFromOnboarding(partner.getUserId()));
        response.setPartnerPersonalities(getPersonalitiesFromOnboarding(partner.getUserId()));
        response.setCompatibilityScore(compatibilityCalculatorService.calculateSimpleScore(currentUser, partner));
        
        // 실시간 온라인 상태 적용
        try {
            var onlineStatus = userStatusService.getUserStatus(partner.getUserId());
            response.setOnlineStatus(onlineStatus.getStatus().toLowerCase());
            response.setLastActiveTime(onlineStatus.getLastSeenText());
        } catch (Exception e) {
            response.setOnlineStatus("offline");
            response.setLastActiveTime("알 수 없음");
        }
        
        return response;
    }

    private SentMatchingRequestResponse convertToSentMatchingRequestResponse(MatchingRequest request) {
        SentMatchingRequestResponse response = new SentMatchingRequestResponse();
        User target = request.getReceiver();
        
        response.setRequestId(request.getRequestId());
        response.setTargetUserId(target.getUserId());
        response.setTargetUserName(target.getEnglishName());
        response.setTargetUserProfileImage(target.getProfileImage());
        response.setTargetUserLocation(target.getLocation() != null ? target.getLocation().getCity() : null);
        response.setTargetUserNativeLanguage(target.getNativeLanguage() != null ? target.getNativeLanguage().getName() : null);
        response.setMessage(request.getMessage());
        response.setStatus(request.getStatus().name());
        response.setSentAt(request.getCreatedAt());
        response.setResponseAt(request.getRespondedAt());
        
        return response;
    }

    private ReceivedMatchingRequestResponse convertToReceivedMatchingRequestResponse(User currentUser, MatchingRequest request) {
        ReceivedMatchingRequestResponse response = new ReceivedMatchingRequestResponse();
        User sender = request.getSender();
        
        response.setRequestId(request.getRequestId());
        response.setSenderUserId(sender.getUserId());
        response.setSenderUserName(sender.getEnglishName());
        response.setSenderUserProfileImage(sender.getProfileImage());
        response.setSenderUserLocation(sender.getLocation() != null ? sender.getLocation().getCity() : null);
        response.setSenderUserNativeLanguage(sender.getNativeLanguage() != null ? sender.getNativeLanguage().getName() : null);
        response.setMessage(request.getMessage());
        response.setStatus(request.getStatus().name());
        response.setReceivedAt(request.getCreatedAt());
        response.setCompatibilityScore(compatibilityCalculatorService.calculateSimpleScore(currentUser, sender));
        
        return response;
    }

    private MatchedPartnerResponse convertToMatchedPartnerResponse(User currentUser, UserMatch match) {
        User partner = match.getUser1().getUserId().equals(currentUser.getUserId()) ? 
                      match.getUser2() : match.getUser1();
        
        MatchedPartnerResponse response = new MatchedPartnerResponse();
        response.setMatchId(match.getMatchId());
        response.setPartnerUserId(partner.getUserId());
        response.setPartnerUserName(partner.getEnglishName());
        response.setPartnerUserProfileImage(partner.getProfileImage());
        response.setPartnerUserLocation(partner.getLocation() != null ? partner.getLocation().getCity() : null);
        response.setPartnerUserNativeLanguage(partner.getNativeLanguage() != null ? partner.getNativeLanguage().getName() : null);
        response.setPartnerUserBio(partner.getSelfBio());
        response.setMatchedAt(match.getCreatedAt());
        response.setCompatibilityScore(compatibilityCalculatorService.calculateSimpleScore(currentUser, partner));
        
        // 실시간 온라인 상태 적용
        try {
            var onlineStatus = userStatusService.getUserStatus(partner.getUserId());
            response.setOnlineStatus(onlineStatus.getStatus().toLowerCase());
            response.setLastActiveTime(onlineStatus.getLastSeenText());
        } catch (Exception e) {
            response.setOnlineStatus("offline");
            response.setLastActiveTime("알 수 없음");
        }
        response.setTotalSessionsCompleted(getTotalSessionsCompleted(currentUser.getUserId(), partner.getUserId()));
        response.setFavoriteTopics(getFavoriteTopicsFromOnboarding(partner.getUserId()));
        
        return response;
    }

    // === AI 기반 스마트 매칭 구현 ===

    @Override
    public Page<RecommendedPartnerResponse> getSmartRecommendations(UUID userId, Pageable pageable) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // AI 기반 스마트 추천 알고리즘
        // 1. 사용자의 과거 매칭 이력 분석
        List<UserMatch> matchHistory = userMatchRepository.findByUser(currentUser);
        
        // 2. 피드백 데이터 분석
        List<MatchingFeedback> feedbackHistory = matchingFeedbackRepository.findByReviewer(currentUser);
        
        // 3. 선호도 패턴 분석하여 가중치 적용
        List<User> smartRecommendations = userRepository.findSmartRecommendations(userId, 
                extractPreferenceWeights(matchHistory, feedbackHistory));
        
        List<RecommendedPartnerResponse> responses = smartRecommendations.stream()
                .map(partner -> {
                    RecommendedPartnerResponse response = convertToRecommendedPartnerResponse(currentUser, partner);
                    // AI 기반 점수로 재계산
                    response.setCompatibilityScore(calculateAICompatibilityScore(currentUser, partner, feedbackHistory));
                    return response;
                })
                .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        
        if (start >= responses.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, responses.size());
        }
        
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Override
    public Page<RecommendedPartnerResponse> getRealTimeMatches(UUID userId, String sessionType) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 실시간 온라인 사용자 중에서 세션 타입에 맞는 파트너 검색
        List<User> realTimeMatches = userRepository.findRealTimeMatches(userId, sessionType);
        
        List<RecommendedPartnerResponse> responses = realTimeMatches.stream()
                .map(partner -> convertToRecommendedPartnerResponse(currentUser, partner))
                .sorted((a, b) -> {
                    // 온라인 시간순으로 정렬 (최근 활동 우선)
                    if ("online".equals(a.getOnlineStatus()) && !"online".equals(b.getOnlineStatus())) return -1;
                    if (!"online".equals(a.getOnlineStatus()) && "online".equals(b.getOnlineStatus())) return 1;
                    return Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore());
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, Pageable.unpaged(), responses.size());
    }

    @Override
    @Transactional
    public void recordMatchingFeedback(UUID userId, UUID partnerId, int qualityScore, String feedback) {
        User reviewer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND REVIEWER"));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND PARTNER"));

        // 활성 매칭 확인
        UserMatch userMatch = userMatchRepository.findActiveMatchBetweenUsers(reviewer, partner)
                .orElseThrow(() -> new IllegalStateException("활성 매칭을 찾을 수 없습니다."));

        MatchingFeedback matchingFeedback = MatchingFeedback.builder()
                .reviewer(reviewer)
                .partner(partner)
                .userMatch(userMatch)
                .overallRating(qualityScore)
                .writtenFeedback(feedback)
                .sessionQualityScore(qualityScore * 20) // 1-5 점수를 1-100 점수로 변환
                .build();

        matchingFeedbackRepository.save(matchingFeedback);
    }

    @Override
    @Transactional
    public void updateMatchingPreferences(UUID userId, AdvancedMatchingFilterRequest preferences) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 사용자 매칭 선호도 업데이트 (User 엔티티에 매칭 선호도 필드 추가 필요)
        // 현재는 로그만 남김
        System.out.println("매칭 선호도 업데이트 - 사용자: " + userId + ", 선호도: " + preferences);
    }

    @Override
    public Object getMatchingStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Map<String, Object> stats = new HashMap<>();
        
        // 매칭 통계 계산
        long totalMatches = userMatchRepository.countByUser(user);
        long activeMatches = userMatchRepository.countActiveMatchesByUser(user);
        long completedSessions = getTotalSessionsCompleted(userId, null);
        
        // 피드백 통계
        List<MatchingFeedback> receivedFeedbacks = matchingFeedbackRepository.findByPartner(user);
        double averageRating = receivedFeedbacks.stream()
                .mapToInt(MatchingFeedback::getOverallRating)
                .average()
                .orElse(0.0);
        
        stats.put("totalMatches", totalMatches);
        stats.put("activeMatches", activeMatches);
        stats.put("completedSessions", completedSessions);
        stats.put("averageRating", Math.round(averageRating * 100.0) / 100.0);
        stats.put("totalFeedbacks", receivedFeedbacks.size());
        stats.put("matchSuccessRate", calculateMatchSuccessRate(user));
        
        return stats;
    }

    @Override
    public Object getMatchingAnalytics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Map<String, Object> analytics = new HashMap<>();
        
        // 알고리즘 성능 분석
        List<MatchingFeedback> feedbacks = matchingFeedbackRepository.findByReviewer(user);
        List<UserMatch> matches = userMatchRepository.findByUser(user);
        
        analytics.put("algorithmVersion", "v2.1");
        analytics.put("totalRecommendations", calculateTotalRecommendations(user));
        analytics.put("acceptanceRate", calculateAcceptanceRate(user));
        analytics.put("feedbackScore", calculateAverageFeedbackScore(feedbacks));
        analytics.put("matchRetentionRate", calculateMatchRetentionRate(matches));
        analytics.put("preferredPartnerTypes", analyzePreferredPartnerTypes(feedbacks));
        
        return analytics;
    }

    @Override
    @Transactional
    public void optimizeMatchingAlgorithm(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 매칭 이력 분석
        List<MatchingFeedback> feedbacks = matchingFeedbackRepository.findByReviewer(user);
        List<UserMatch> matches = userMatchRepository.findByUser(user);
        
        // 선호도 패턴 분석
        Map<String, Double> preferenceWeights = analyzeUserPreferences(feedbacks, matches);
        
        // 알고리즘 가중치 업데이트 (현재는 로그만)
        System.out.println("사용자 " + userId + "의 알고리즘 최적화 완료. 가중치: " + preferenceWeights);
    }

    @Override
    public Page<RecommendedPartnerResponse> getScheduleBasedMatches(UUID userId, String dayOfWeek, String timeSlot, Pageable pageable) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 스케줄 기반 매칭 (온보딩 데이터의 스케줄 정보 활용)
        List<User> scheduleMatches = userRepository.findScheduleBasedMatches(userId, dayOfWeek, timeSlot);
        
        List<RecommendedPartnerResponse> responses = scheduleMatches.stream()
                .map(partner -> convertToRecommendedPartnerResponse(currentUser, partner))
                .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        
        if (start >= responses.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, responses.size());
        }
        
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Override
    public Page<RecommendedPartnerResponse> getLanguageExchangePartners(UUID userId, Pageable pageable) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 언어 교환 가능한 파트너 (서로의 언어를 배울 수 있는)
        List<User> languageExchangePartners = userRepository.findLanguageExchangePartners(userId);
        
        List<RecommendedPartnerResponse> responses = languageExchangePartners.stream()
                .map(partner -> {
                    RecommendedPartnerResponse response = convertToRecommendedPartnerResponse(currentUser, partner);
                    // 언어 교환 보너스 점수 추가
                    response.setCompatibilityScore(response.getCompatibilityScore() + 15.0);
                    return response;
                })
                .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        
        if (start >= responses.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, responses.size());
        }
        
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    // === 매칭 대기열 관리 ===

    @Override
    @Transactional
    public void addToMatchingQueue(UUID userId, String sessionType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 기존 대기열 항목 확인
        Optional<MatchingQueue> existingQueue = matchingQueueRepository.findByUserAndStatus(
                user, MatchingQueue.QueueStatus.WAITING);
        
        if (existingQueue.isPresent()) {
            throw new IllegalStateException("이미 대기열에 참가되어 있습니다.");
        }

        MatchingQueue queueEntry = MatchingQueue.builder()
                .user(user)
                .sessionType(MatchingQueue.SessionType.valueOf(sessionType))
                .status(MatchingQueue.QueueStatus.WAITING)
                .joinedAt(LocalDateTime.now())
                .priorityScore(calculatePriorityScore(user))
                .estimatedWaitMinutes(calculateEstimatedWaitTime())
                .build();

        matchingQueueRepository.save(queueEntry);
    }

    @Override
    @Transactional
    public void removeFromMatchingQueue(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        matchingQueueRepository.findByUserAndStatus(user, MatchingQueue.QueueStatus.WAITING)
                .ifPresent(queueEntry -> {
                    queueEntry.updateStatus(MatchingQueue.QueueStatus.CANCELLED);
                    matchingQueueRepository.save(queueEntry);
                });
    }

    @Override
    public Object getMatchingQueueStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Map<String, Object> queueStatus = new HashMap<>();
        
        Optional<MatchingQueue> queueEntry = matchingQueueRepository.findByUserAndStatus(
                user, MatchingQueue.QueueStatus.WAITING);
        
        if (queueEntry.isPresent()) {
            MatchingQueue entry = queueEntry.get();
            queueStatus.put("inQueue", true);
            queueStatus.put("position", calculateQueuePosition(entry));
            queueStatus.put("estimatedWaitTime", entry.getEstimatedWaitMinutes());
            queueStatus.put("joinedAt", entry.getJoinedAt());
            queueStatus.put("sessionType", entry.getSessionType().name());
            queueStatus.put("priorityScore", entry.getPriorityScore());
        } else {
            queueStatus.put("inQueue", false);
        }
        
        return queueStatus;
    }

    // === Helper Methods for Advanced Features ===

    private Map<String, Double> extractPreferenceWeights(List<UserMatch> matchHistory, List<MatchingFeedback> feedbackHistory) {
        Map<String, Double> weights = new HashMap<>();
        
        // 기본 가중치 설정
        weights.put("language_compatibility", 0.3);
        weights.put("age_compatibility", 0.2);
        weights.put("location_compatibility", 0.15);
        weights.put("personality_compatibility", 0.25);
        weights.put("schedule_compatibility", 0.1);
        
        // 피드백 데이터 기반 가중치 조정
        if (!feedbackHistory.isEmpty()) {
            double avgCommunicationRating = feedbackHistory.stream()
                    .filter(f -> f.getCommunicationRating() != null)
                    .mapToInt(MatchingFeedback::getCommunicationRating)
                    .average().orElse(3.0);
            
            if (avgCommunicationRating > 4.0) {
                weights.put("language_compatibility", weights.get("language_compatibility") + 0.1);
            }
        }
        
        return weights;
    }

    private double calculateAICompatibilityScore(User currentUser, User partner, List<MatchingFeedback> feedbackHistory) {
        // 기본 호환성 점수
        double baseScore = compatibilityCalculatorService.calculateSimpleScore(currentUser, partner);
        
        // AI 기반 조정
        double aiAdjustment = 0.0;
        
        // 피드백 이력 기반 조정
        if (!feedbackHistory.isEmpty()) {
            double avgFeedbackScore = feedbackHistory.stream()
                    .mapToInt(MatchingFeedback::getOverallRating)
                    .average().orElse(3.0);
            
            if (avgFeedbackScore > 4.0) {
                aiAdjustment += 10.0; // 고평점 사용자는 보너스
            }
        }
        
        return Math.min(100.0, baseScore + aiAdjustment);
    }

    private double calculateMatchSuccessRate(User user) {
        List<UserMatch> matches = userMatchRepository.findByUser(user);
        if (matches.isEmpty()) return 0.0;
        
        long activeMatches = matches.stream()
                .mapToLong(match -> match.getIsActive() ? 1L : 0L)
                .sum();
        
        return (double) activeMatches / matches.size() * 100.0;
    }

    private int calculateTotalRecommendations(User user) {
        // 추천 이력 계산 (매칭 요청 수로 대체)
        return (int) matchingRequestRepository.countByReceiver(user);
    }

    private double calculateAcceptanceRate(User user) {
        // 추천 수락률 계산 (받은 요청 중 수락한 비율)
        long totalReceived = matchingRequestRepository.countByReceiver(user);
        long totalAccepted = matchingRequestRepository.countByReceiverAndStatus(user, MatchingStatus.ACCEPTED);
        return totalReceived > 0 ? (double) totalAccepted / totalReceived : 0.0;
    }

    private double calculateAverageFeedbackScore(List<MatchingFeedback> feedbacks) {
        return feedbacks.stream()
                .mapToInt(MatchingFeedback::getOverallRating)
                .average()
                .orElse(0.0);
    }

    private double calculateMatchRetentionRate(List<UserMatch> matches) {
        if (matches.isEmpty()) return 0.0;
        
        long activeMatches = matches.stream()
                .mapToLong(match -> match.getIsActive() ? 1L : 0L)
                .sum();
        
        return (double) activeMatches / matches.size() * 100.0;
    }

    private Map<String, Integer> analyzePreferredPartnerTypes(List<MatchingFeedback> feedbacks) {
        Map<String, Integer> partnerTypes = new HashMap<>();
        
        // 높은 평점을 받은 파트너들의 특성 분석
        feedbacks.stream()
                .filter(f -> f.getOverallRating() >= 4)
                .forEach(f -> {
                    User partner = f.getPartner();
                    String nativeLanguage = partner.getNativeLanguage() != null ? 
                            partner.getNativeLanguage().getName() : "Unknown";
                    partnerTypes.merge(nativeLanguage, 1, Integer::sum);
                });
        
        return partnerTypes;
    }

    private Map<String, Double> analyzeUserPreferences(List<MatchingFeedback> feedbacks, List<UserMatch> matches) {
        Map<String, Double> preferences = new HashMap<>();
        
        // 피드백 점수 기반 선호도 분석
        double avgLanguageRating = feedbacks.stream()
                .filter(f -> f.getLanguageLevelRating() != null)
                .mapToInt(MatchingFeedback::getLanguageLevelRating)
                .average().orElse(3.0);
        
        double avgCommunicationRating = feedbacks.stream()
                .filter(f -> f.getCommunicationRating() != null)
                .mapToInt(MatchingFeedback::getCommunicationRating)
                .average().orElse(3.0);
        
        preferences.put("language_importance", avgLanguageRating / 5.0);
        preferences.put("communication_importance", avgCommunicationRating / 5.0);
        
        return preferences;
    }

    private int calculatePriorityScore(User user) {
        int priority = 1; // 기본 우선순위
        
        // 프리미엄 사용자는 우선순위 증가
        // 프리미엄 사용자 확인 (현재는 기본 구현)
        // 실제로는 사용자 등급 필드를 확인해야 함
        
        // 매칭 성공률이 높은 사용자는 우선순위 증가
        double successRate = calculateMatchSuccessRate(user);
        if (successRate > 80.0) {
            priority += 2;
        }
        
        return priority;
    }

    private int calculateEstimatedWaitTime() {
        // 현재 대기열 상황 기반 대기시간 계산
        long waitingCount = matchingQueueRepository.countByStatus(MatchingQueue.QueueStatus.WAITING);
        
        // 대기 인원에 따른 예상 대기시간 (분)
        return (int) Math.max(1, waitingCount / 5 * 3); // 5명당 약 3분
    }

    private int calculateQueuePosition(MatchingQueue queueEntry) {
        return (int) (matchingQueueRepository.countByStatusAndJoinedAtBefore(
                MatchingQueue.QueueStatus.WAITING, 
                queueEntry.getJoinedAt()) + 1);
    }

    // === Helper Methods for Onboarding Data ===

    private List<RecommendedPartnerResponse.TargetLanguageInfo> getTargetLanguagesFromOnboarding(UUID userId) {
        List<OnboardingLangLevel> languageLevels = onboardingLangLevelRepository.findByUsrId(userId);

        if (languageLevels == null || languageLevels.isEmpty()) {
            return Collections.emptyList();
        }

        return languageLevels.stream()
                .map(level -> {
                    RecommendedPartnerResponse.TargetLanguageInfo info = new RecommendedPartnerResponse.TargetLanguageInfo();
                    info.setLanguageName(level.getLanguage() != null ? level.getLanguage().getName() : null);
                    info.setCurrentLevel(level.getCurrentLevel() != null ? level.getCurrentLevel().getName() : null);
                    info.setTargetLevel(level.getTargetLevel() != null ? level.getTargetLevel().getName() : null);
                    return info;
                })
                .filter(info -> info.getLanguageName() != null)
                .toList();
    }

    private List<String> getInterestsFromOnboarding(UUID userId) {
        LinkedHashSet<String> interests = new LinkedHashSet<>();

        List<Integer> motivationIds = onboardingMotivationRepository.findByUsrId(userId).stream()
                .map(OnboardingMotivation::getMotivationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!motivationIds.isEmpty()) {
            motivationRepository.findAllById(motivationIds).stream()
                    .map(Motivation::getName)
                    .filter(StringUtils::hasText)
                    .forEach(interests::add);
        }

        List<Integer> topicIds = onboardingTopicRepository.findByUsrId(userId).stream()
                .map(OnboardingTopic::getTopicId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!topicIds.isEmpty()) {
            topicRepository.findAllById(topicIds).stream()
                    .map(Topic::getName)
                    .filter(StringUtils::hasText)
                    .forEach(interests::add);
        }

        List<Integer> learningStyleIds = onboardingLearningStyleRepository.findByUsrId(userId).stream()
                .map(OnboardingLearningStyle::getLearningStyleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!learningStyleIds.isEmpty()) {
            learningStyleRepository.findAllById(learningStyleIds).stream()
                    .map(LearningStyle::getName)
                    .filter(StringUtils::hasText)
                    .forEach(interests::add);
        }

        List<Integer> expectationIds = onboardingLearningExpectationRepository.findByUsrId(userId).stream()
                .map(expectation -> expectation.getId().getLearningExpectationId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!expectationIds.isEmpty()) {
            learningExpectationRepository.findAllById(expectationIds).stream()
                    .map(LearningExpectation::getLearningExpectationName)
                    .filter(StringUtils::hasText)
                    .forEach(interests::add);
        }

        return new ArrayList<>(interests);
    }

    private List<String> getPersonalitiesFromOnboarding(UUID userId) {
        List<Integer> personalityIds = onboardingPartnerRepository.findByUsrId(userId).stream()
                .map(OnboardingPartner::getPartnerPersonalityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (personalityIds.isEmpty()) {
            return Collections.emptyList();
        }

        return partnerPersonalityRepository.findAllById(personalityIds).stream()
                .map(PartnerPersonality::getName)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String getFavoriteTopicsFromOnboarding(UUID userId) {
        List<Integer> topicIds = onboardingTopicRepository.findByUsrId(userId).stream()
                .map(OnboardingTopic::getTopicId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (topicIds.isEmpty()) {
            return null;
        }

        List<String> topicNames = topicRepository.findAllById(topicIds).stream()
                .map(Topic::getName)
                .filter(StringUtils::hasText)
                .toList();

        if (topicNames.isEmpty()) {
            return null;
        }

        return String.join(", ", topicNames);
    }

    private int getTotalSessionsCompleted(UUID userId, UUID partnerId) {
        if (partnerId != null) {
            Long pairCompleted = sessionRepository.countCompletedSessionsBetweenUsers(
                    userId,
                    partnerId,
                    SessionStatus.COMPLETED
            );
            return pairCompleted != null ? pairCompleted.intValue() : 0;
        }

        Long completed = sessionRepository.countCompletedSessionsByUserId(userId, SessionStatus.COMPLETED);
        return completed != null ? completed.intValue() : 0;
    }
}
