package com.studymate.domain.onboard.service;

import com.studymate.domain.onboard.domain.dto.request.LearningExceptionRequest;
import com.studymate.domain.onboard.domain.dto.request.LearningStyleRequest;
import com.studymate.domain.onboard.domain.dto.request.MotivationRequest;
import com.studymate.domain.onboard.domain.dto.request.TopicRequest;
import com.studymate.domain.onboard.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboard.domain.dto.response.LearningStyleResponse;
import com.studymate.domain.onboard.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboard.domain.dto.response.TopicResponse;

import java.util.List;
import java.util.UUID;

public interface OnboardInterestService {
    void saveMotivation(UUID userId,MotivationRequest req);
    void saveTopic(UUID userId,TopicRequest req);
    void saveLearningStyle(UUID userId,LearningStyleRequest req);
    void saveLearningExpectation(UUID userId,LearningExceptionRequest req);
    List<MotivationResponse> getAllMotivation();
    List<TopicResponse> getAllTopic();
    List<LearningStyleResponse> getAllLearningStyle();
    List<LearningExpectationResponse> getAllLearningExpectationType();

}
