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
import com.studymate.domain.onboarding.domain.type.LearningExpectionType;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardInterestServiceImpl implements OnboardInterestService {

    private final OnboardMotivationRepository onboardMotivationRepository;
    private final OnboardTopicRepository onboardTopicRepository;
    private final OnboardLearningStyleRepository onboardLearningStyleRepository;
    private final UserRepository userRepository;
    private final MotivationRepository motivationRepository;
    private final TopicRepository topicRepository;
    private final LearningStyleRepository learningStyleRepository;

    @Override
    @Transactional
    public void saveMotivation(UUID userId,MotivationRequest req){
        List<Integer> motivationIds = req.motivationIds();
        List<OnboardMotivation> onboardMotivations = motivationIds.stream()
                .map(motivationId -> OnboardMotivation.builder()
                        .id(new OnboardMotivationId(userId, motivationId))
                        .build())
                .collect(Collectors.toList());

        onboardMotivationRepository.saveAll(onboardMotivations);
    }

    @Override
    @Transactional
    public void saveTopic(UUID userId,TopicRequest req){
        List<Integer> topicIds = req.topicIds();
        List<OnboardTopic> onboardTopics = topicIds.stream()
                .map(topicId -> OnboardTopic.builder()
                        .id(new OnboardTopicId(userId, topicId))
                        .build())
                .collect(Collectors.toList());

        onboardTopicRepository.saveAll(onboardTopics);
    }

    @Override
    @Transactional
    public void saveLearningStyle(UUID userId,LearningStyleRequest req) {
        List<Integer> learningStyleIds = req.learningStyleIds();
        List<OnboardLearningStyle> onboardLearningStyles = learningStyleIds.stream()
                .map(learningStyleId -> OnboardLearningStyle.builder()
                        .id(new OnboardLearningStyleId(userId, learningStyleId))
                        .build())
                .collect(Collectors.toList());
        onboardLearningStyleRepository.saveAll(onboardLearningStyles);
    }

    @Override
    @Transactional
    public void saveLearningExpectation(UUID userId, LearningExceptionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER NOT FOUND"));

        // 배열에서 첫 번째 ID를 사용하여 LearningExpectionType 매핑
        if (req.learningExpectationIds() != null && !req.learningExpectationIds().isEmpty()) {
            Integer firstId = req.learningExpectationIds().get(0);
            
            // ID를 LearningExpectionType으로 매핑 (임시 로직)
            LearningExpectionType learningExpectionType = mapIdToLearningExpectionType(firstId);
            
            user.setLearningExpectionType(learningExpectionType);
            userRepository.save(user);
        }
    }
    
    private LearningExpectionType mapIdToLearningExpectionType(Integer id) {
        // TODO: 실제 매핑 로직은 데이터베이스 구조에 따라 결정
        // 현재는 임시로 순서대로 매핑
        return switch (id) {
            case 1 -> LearningExpectionType.HABIT;
            case 2 -> LearningExpectionType.CONFIDENCE;
            case 3 -> LearningExpectionType.CUSTOMIZED_METHOD;
            case 4 -> LearningExpectionType.PRACTICAL_CONVERSATION;
            default -> LearningExpectionType.HABIT; // 기본값
        };
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
    return Arrays.stream(LearningExpectionType.values())
            .map(e -> new LearningExpectationResponse(e.name(), e.getDescription()))
            .toList();
}







}
