package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardGroupSize;
import com.studymate.domain.onboarding.entity.OnboardGroupSizeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardGroupSizeRepository extends JpaRepository <OnboardGroupSize, OnboardGroupSizeId> {
}
