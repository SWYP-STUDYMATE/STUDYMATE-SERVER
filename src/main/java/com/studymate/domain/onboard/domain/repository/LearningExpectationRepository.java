package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.LearningExpectation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningExpectationRepository extends JpaRepository<LearningExpectation, Integer> {
}