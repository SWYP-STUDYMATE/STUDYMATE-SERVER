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
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.domain.user.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboard/interest")
public class OnbaordInterestController {

    private final OnboardInterestService onboardInterestService;
    private final JwtUtils jwtUtils;

    @PostMapping("/motivation")
    public void saveMotivation(@AuthenticationPrincipal CustomUserDetails principal,
                               @RequestBody MotivationRequest req
    ){
        UUID userId = principal.getUuid();
        onboardInterestService.saveMotivation(userId,req);
    }

    @PostMapping("/topic")
    public void saveTopic(@AuthenticationPrincipal CustomUserDetails principal,
                          @RequestBody TopicRequest req
    ){
        UUID userId = principal.getUuid();
        onboardInterestService.saveTopic(userId,req);
    }

    @PostMapping("/learning-style")
    public void saveLearningStyle(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody LearningStyleRequest req
    ){
        UUID userId = principal.getUuid();
        onboardInterestService.saveLearningStyle(userId,req);
    }

    @PostMapping("/learning-expectation")
    public void saveLearningExpectation(@AuthenticationPrincipal CustomUserDetails principal,
                                        @RequestBody LearningExceptionRequest req
    ){
        UUID userId = principal.getUuid();
        onboardInterestService.saveLearningExpectation(userId,req);
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
