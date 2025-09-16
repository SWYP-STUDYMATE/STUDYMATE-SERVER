package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingSchedule;
import com.studymate.domain.onboarding.entity.OnboardingScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingScheduleRepository extends JpaRepository<OnboardingSchedule, OnboardingScheduleId> {
    
    @Query("SELECT os FROM OnboardingSchedule os WHERE os.id.userId = :userId")
    List<OnboardingSchedule> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingSchedule os WHERE os.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
