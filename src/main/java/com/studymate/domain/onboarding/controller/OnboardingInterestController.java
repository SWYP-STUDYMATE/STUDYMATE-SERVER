package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.LearningExceptionRequest;
import com.studymate.domain.onboarding.domain.dto.request.LearningStyleRequest;
import com.studymate.domain.onboarding.domain.dto.request.MotivationRequest;
import com.studymate.domain.onboarding.domain.dto.request.TopicRequest;
import com.studymate.domain.onboarding.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboarding.domain.dto.response.LearningStyleResponse;
import com.studymate.domain.onboarding.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboarding.domain.dto.response.TopicResponse;
import com.studymate.domain.onboarding.service.OnboardingInterestService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.auth.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/onboarding/interest")
public class OnboardingInterestController {

    private final OnboardingInterestService onboardingInterestService;
    private final JwtUtils jwtUtils;

    @PostMapping("/motivation")
    public void saveMotivation(@AuthenticationPrincipal CustomUserDetails principal,
                               @RequestBody MotivationRequest req
    ){
        UUID userId = principal.getUuid();
        onboardingInterestService.saveMotivation(userId,req);
    }

    @PostMapping("/topic")
    public void saveTopic(@AuthenticationPrincipal CustomUserDetails principal,
                          @RequestBody TopicRequest req
    ){
        UUID userId = principal.getUuid();
        onboardingInterestService.saveTopic(userId,req);
    }

    @PostMapping("/learning-style")
    public void saveLearningStyle(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestBody LearningStyleRequest req
    ){
        UUID userId = principal.getUuid();
        onboardingInterestService.saveLearningStyle(userId,req);
    }

    @PostMapping("/learning-expectation")
    public void saveLearningExpectation(@AuthenticationPrincipal CustomUserDetails principal,
                                        @RequestBody LearningExceptionRequest req
    ){
        UUID userId = principal.getUuid();
        onboardingInterestService.saveLearningExpectation(userId,req);
    }

    @GetMapping("/motivations")
    public List<MotivationResponse> getAllMotivation(){
        return onboardingInterestService.getAllMotivation();
    }

    @GetMapping("/topics")
    public List<TopicResponse> getAllTopic(){
        return onboardingInterestService.getAllTopic();
    }

    @GetMapping("/learning-styles")
    public List<LearningStyleResponse> getAllLearningStyle(){
        return onboardingInterestService.getAllLearningStyle();
    }

    @GetMapping("/learning-expectations")
    public List<LearningExpectationResponse> getAllLearningExpectationType() {
        return onboardingInterestService.getAllLearningExpectationType();
    }


}
