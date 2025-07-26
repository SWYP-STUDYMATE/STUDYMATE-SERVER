package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardLangLevel;
import com.studymate.domain.onboarding.entity.OnboardLangLevelId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardLangLevelRepository extends JpaRepository<OnboardLangLevel, OnboardLangLevelId> {
}
