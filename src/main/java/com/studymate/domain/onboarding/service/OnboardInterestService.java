package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.LearningExceptionRequest;
import com.studymate.domain.onboarding.domain.dto.request.LearningStyleRequest;
import com.studymate.domain.onboarding.domain.dto.request.MotivationRequest;
import com.studymate.domain.onboarding.domain.dto.request.TopicRequest;
import com.studymate.domain.onboarding.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboarding.domain.dto.response.LearningStyleResponse;
import com.studymate.domain.onboarding.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboarding.domain.dto.response.TopicResponse;

import java.util.List;

public interface OnboardInterestService {
    void saveMotivation(MotivationRequest req);
    void saveTopic(TopicRequest req);
    void saveLearningStyle(LearningStyleRequest req);
    void saveLearningExpectation(LearningExceptionRequest req);
    List<MotivationResponse> getAllMotivation();
    List<TopicResponse> getAllTopic();
    List<LearningStyleResponse> getAllLearningStyle();
    List<LearningExpectationResponse> getAllLearningExpectationType();
//    LearningExpectionType[] getAllLearningExpectationType();

}
