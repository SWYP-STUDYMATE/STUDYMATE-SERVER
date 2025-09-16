package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingGroupSize;
import com.studymate.domain.onboarding.entity.OnboardingGroupSizeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingGroupSizeRepository extends JpaRepository <OnboardingGroupSize, OnboardingGroupSizeId> {
    
    @Query("SELECT ogs FROM OnboardingGroupSize ogs WHERE ogs.id.userId = :userId")
    List<OnboardingGroupSize> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingGroupSize ogs WHERE ogs.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
