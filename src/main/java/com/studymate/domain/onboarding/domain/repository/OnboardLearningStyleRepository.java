package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardLearningStyle;
import com.studymate.domain.onboarding.entity.OnboardLearningStyleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardLearningStyleRepository extends JpaRepository<OnboardLearningStyle, OnboardLearningStyleId> {
}
