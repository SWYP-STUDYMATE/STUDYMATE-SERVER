package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardTopic;
import com.studymate.domain.onboarding.entity.OnboardTopicId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardTopicRepository extends JpaRepository<OnboardTopic, OnboardTopicId> {
}
