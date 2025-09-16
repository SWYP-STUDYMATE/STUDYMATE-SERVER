package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingTopic;
import com.studymate.domain.onboarding.entity.OnboardingTopicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingTopicRepository extends JpaRepository<OnboardingTopic, OnboardingTopicId> {
    
    @Query("SELECT ot FROM OnboardingTopic ot WHERE ot.id.userId = :userId")
    List<OnboardingTopic> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardingTopic ot WHERE ot.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
