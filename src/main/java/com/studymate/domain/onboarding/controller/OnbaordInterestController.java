package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.LearningExceptionRequest;
import com.studymate.domain.onboarding.domain.dto.request.LearningStyleRequest;
import com.studymate.domain.onboarding.domain.dto.request.MotivationRequest;
import com.studymate.domain.onboarding.domain.dto.request.TopicRequest;
import com.studymate.domain.onboarding.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboarding.domain.dto.response.LearningStyleResponse;
import com.studymate.domain.onboarding.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboarding.domain.dto.response.TopicResponse;
import com.studymate.domain.onboarding.service.OnboardInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboard/interest")
public class OnbaordInterestController {

    private final OnboardInterestService onboardInterestService;

    @PostMapping("/motivation")
    public void saveMotivation(@RequestBody MotivationRequest req){
        onboardInterestService.saveMotivation(req);
    }

    @PostMapping("/topic")
    public void saveTopic(@RequestBody TopicRequest req){
        onboardInterestService.saveTopic(req);
    }

    @PostMapping("/learning-style")
    public void saveLearningStyle(@RequestBody LearningStyleRequest req){
        onboardInterestService.saveLearningStyle(req);
    }

    @PostMapping("/learning-expectation")
    public void saveLearningExpectation(@RequestBody LearningExceptionRequest req){
        onboardInterestService.saveLearningExpectation(req);
    }

    @GetMapping("/motivations")
    public List<MotivationResponse> getAllMotivation(){
        return onboardInterestService.getAllMotivation();
    }

    @GetMapping("/topics")
    public List<TopicResponse> getAllTopic(){
        return onboardInterestService.getAllTopic();
    }

    @GetMapping("/learning-styles")
    public List<LearningStyleResponse> getAllLearningStyle(){
        return onboardInterestService.getAllLearningStyle();
    }

    @GetMapping("/learning-expectations")
    public List<LearningExpectationResponse> getAllLearningExpectationType() {
        return onboardInterestService.getAllLearningExpectationType();
    }


}
