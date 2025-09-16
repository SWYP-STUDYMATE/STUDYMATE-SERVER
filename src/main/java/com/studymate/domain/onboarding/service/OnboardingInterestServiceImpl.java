package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.LearningExceptionRequest;
import com.studymate.domain.onboarding.domain.dto.request.LearningStyleRequest;
import com.studymate.domain.onboarding.domain.dto.request.MotivationRequest;
import com.studymate.domain.onboarding.domain.dto.request.TopicRequest;
import com.studymate.domain.onboarding.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboarding.domain.dto.response.LearningStyleResponse;
import com.studymate.domain.onboarding.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboarding.domain.dto.response.TopicResponse;
import com.studymate.domain.onboarding.domain.repository.*;
import com.studymate.domain.onboarding.entity.*;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingInterestServiceImpl implements OnboardingInterestService {

    private final OnboardingMotivationRepository onboardingMotivationRepository;
    private final OnboardingTopicRepository onboardingTopicRepository;
    private final OnboardingLearningStyleRepository onboardingLearningStyleRepository;
    private final OnboardingLearningExpectationRepository onboardingLearningExpectationRepository;
    private final UserRepository userRepository;
    private final MotivationRepository motivationRepository;
    private final TopicRepository topicRepository;
    private final LearningStyleRepository learningStyleRepository;
    private final LearningExpectationRepository learningExpectationRepository;

    @Override
    @Transactional
    public void saveMotivation(UUID userId,MotivationRequest req){
        List<Integer> motivationIds = req.motivationIds();
        List<OnboardingMotivation> onboardMotivations = motivationIds.stream()
                .map(motivationId -> OnboardingMotivation.builder()
                        .id(new OnboardingMotivationId(userId, motivationId))
                        .build())
                .collect(Collectors.toList());

        onboardingMotivationRepository.saveAll(onboardMotivations);
    }

    @Override
    @Transactional
    public void saveTopic(UUID userId,TopicRequest req){
        List<Integer> topicIds = req.topicIds();
        List<OnboardingTopic> onboardTopics = topicIds.stream()
                .map(topicId -> OnboardingTopic.builder()
                        .id(new OnboardingTopicId(userId, topicId))
                        .build())
                .collect(Collectors.toList());

        onboardingTopicRepository.saveAll(onboardTopics);
    }

    @Override
    @Transactional
    public void saveLearningStyle(UUID userId,LearningStyleRequest req) {
        List<Integer> learningStyleIds = req.learningStyleIds();
        List<OnboardingLearningStyle> onboardLearningStyles = learningStyleIds.stream()
                .map(learningStyleId -> OnboardingLearningStyle.builder()
                        .id(new OnboardingLearningStyleId(userId, learningStyleId))
                        .build())
                .collect(Collectors.toList());
        onboardingLearningStyleRepository.saveAll(onboardLearningStyles);
    }

    @Override
    @Transactional
    public void saveLearningExpectation(UUID userId, LearningExceptionRequest req) {
        // LearningExpectation ID들 검증
        Set<Integer> learningExpectationIds = new HashSet<>(req.learningExpectationIds());
        Map<Integer, LearningExpectation> learningExpectationMap = learningExpectationRepository
                .findAllById(learningExpectationIds)
                .stream()
                .collect(Collectors.toMap(LearningExpectation::getLearningExpectationId, Function.identity()));
        
        // OnboardingLearningExpectation 엔티티들 생성 및 저장
        List<OnboardingLearningExpectation> onboardLearningExpectations = req.learningExpectationIds().stream()
                .map(expectationId -> {
                    LearningExpectation learningExpectation = learningExpectationMap.get(expectationId);
                    if (learningExpectation == null) {
                        throw new NotFoundException("LEARNING EXPECTATION NOT FOUND: " + expectationId);
                    }
                    
                    OnboardingLearningExpectationId id = new OnboardingLearningExpectationId(userId, expectationId);
                    return OnboardingLearningExpectation.builder()
                            .id(id)
                            .build();
                })
                .collect(Collectors.toList());
                
        onboardingLearningExpectationRepository.saveAll(onboardLearningExpectations);
    }

    @Override
    public List<MotivationResponse> getAllMotivation() {
        return motivationRepository.findAll().stream()
                .map(m->new MotivationResponse(
                        m.getMotivationId(),
                        m.getMotivationName()
                ))
                .toList();
    }

    @Override
    public List<TopicResponse> getAllTopic() {
        return topicRepository.findAll().stream()
                .map(t-> new TopicResponse(
                        t.getTopicId(),
                        t.getTopicName()
                ))
                .toList();
    }

    @Override
    public List<LearningStyleResponse> getAllLearningStyle() {
        return learningStyleRepository.findAll().stream()
                .map(l-> new LearningStyleResponse(
                        l.getLearningStyleId(),
                        l.getLearningStyleName()
                ))
                .toList();
    }


@Override
public List<LearningExpectationResponse> getAllLearningExpectationType() {
    return learningExpectationRepository.findAll().stream()
            .map(e -> new LearningExpectationResponse(
                    e.getLearningExpectationId(),
                    e.getLearningExpectationName()
            ))
            .toList();
}







}
