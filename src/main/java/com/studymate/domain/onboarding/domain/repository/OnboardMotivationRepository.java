package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardMotivation;
import com.studymate.domain.onboarding.entity.OnboardMotivationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardMotivationRepository extends JpaRepository<OnboardMotivation, OnboardMotivationId> {
}
