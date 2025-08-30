package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingPersonality;
import com.studymate.domain.onboarding.entity.OnboardPartnerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingPersonalityRepository extends JpaRepository<OnboardingPersonality, OnboardPartnerId> {
    
    @Query("SELECT op FROM OnboardingPersonality op WHERE op.id.userId = :userId")
    List<OnboardingPersonality> findByUserId(@Param("userId") UUID userId);
}