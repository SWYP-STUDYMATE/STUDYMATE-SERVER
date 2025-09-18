package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingLearningExpectation;
import com.studymate.domain.onboarding.entity.OnboardingLearningExpectationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingLearningExpectationRepository extends JpaRepository<OnboardingLearningExpectation, OnboardingLearningExpectationId> {

    @Query("SELECT ole FROM OnboardingLearningExpectation ole WHERE ole.id.userId = :userId")
    List<OnboardingLearningExpectation> findByUsrId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM OnboardingLearningExpectation ole WHERE ole.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
