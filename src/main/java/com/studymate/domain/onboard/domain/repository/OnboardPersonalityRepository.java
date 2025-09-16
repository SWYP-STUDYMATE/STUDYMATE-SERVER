package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardPersonality;
import com.studymate.domain.onboard.entity.OnboardPartnerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardPersonalityRepository extends JpaRepository<OnboardPersonality, OnboardPartnerId> {
    
    @Query("SELECT op FROM OnboardPersonality op WHERE op.id.userId = :userId")
    List<OnboardPersonality> findByUserId(@Param("userId") UUID userId);
}