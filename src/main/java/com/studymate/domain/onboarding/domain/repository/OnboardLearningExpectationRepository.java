package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardLearningExpectation;
import com.studymate.domain.onboarding.entity.OnboardLearningExpectationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardLearningExpectationRepository extends JpaRepository<OnboardLearningExpectation, OnboardLearningExpectationId> {
}