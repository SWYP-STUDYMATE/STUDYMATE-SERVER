package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingStudyGoal;
import com.studymate.domain.onboarding.entity.OnboardingMotivationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingStudyGoalRepository extends JpaRepository<OnboardingStudyGoal, OnboardingMotivationId> {
    
    @Query("SELECT osg FROM OnboardingStudyGoal osg WHERE osg.id.userId = :userId")
    List<OnboardingStudyGoal> findByUserId(@Param("userId") UUID userId);
}