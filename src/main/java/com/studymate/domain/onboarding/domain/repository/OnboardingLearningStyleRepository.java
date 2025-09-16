package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingLearningStyle;
import com.studymate.domain.onboarding.entity.OnboardingLearningStyleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingLearningStyleRepository extends JpaRepository<OnboardingLearningStyle, OnboardingLearningStyleId> {
    
    @Query("SELECT ols FROM OnboardingLearningStyle ols WHERE ols.id.userId = :userId")
    List<OnboardingLearningStyle> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingLearningStyle ols WHERE ols.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
