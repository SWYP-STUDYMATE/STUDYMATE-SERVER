package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.LearningExpectation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningExpectationRepository extends JpaRepository<LearningExpectation, Integer> {
}