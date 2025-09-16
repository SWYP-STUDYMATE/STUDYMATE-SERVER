package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardMotivation;
import com.studymate.domain.onboard.entity.OnboardMotivationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardMotivationRepository extends JpaRepository<OnboardMotivation, OnboardMotivationId> {
    
    @Query("SELECT om FROM OnboardMotivation om WHERE om.id.userId = :userId")
    List<OnboardMotivation> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardMotivation om WHERE om.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
