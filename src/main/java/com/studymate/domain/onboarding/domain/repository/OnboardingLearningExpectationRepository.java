package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingLearningExpectation;
import com.studymate.domain.onboarding.entity.OnboardingLearningExpectationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingLearningExpectationRepository extends JpaRepository<OnboardingLearningExpectation, OnboardingLearningExpectationId> {
}