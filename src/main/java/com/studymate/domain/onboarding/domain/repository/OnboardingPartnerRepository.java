package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingPartner;
import com.studymate.domain.onboarding.entity.OnboardingPartnerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingPartnerRepository extends JpaRepository<OnboardingPartner, OnboardingPartnerId> {
    
    @Query("SELECT op FROM OnboardingPartner op WHERE op.id.userId = :userId")
    List<OnboardingPartner> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingPartner op WHERE op.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
