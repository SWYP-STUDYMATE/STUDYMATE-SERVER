package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardLearningExpectation;
import com.studymate.domain.onboard.entity.OnboardLearningExpectationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardLearningExpectationRepository extends JpaRepository<OnboardLearningExpectation, OnboardLearningExpectationId> {
}