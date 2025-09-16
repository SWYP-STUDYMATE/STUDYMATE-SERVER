package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingMotivation;
import com.studymate.domain.onboarding.entity.OnboardingMotivationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingMotivationRepository extends JpaRepository<OnboardingMotivation, OnboardingMotivationId> {
    
    @Query("SELECT om FROM OnboardingMotivation om WHERE om.id.userId = :userId")
    List<OnboardingMotivation> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingMotivation om WHERE om.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
