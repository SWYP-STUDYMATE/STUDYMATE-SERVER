package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardStudyGoal;
import com.studymate.domain.onboard.entity.OnboardMotivationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardStudyGoalRepository extends JpaRepository<OnboardStudyGoal, OnboardMotivationId> {
    
    @Query("SELECT osg FROM OnboardStudyGoal osg WHERE osg.id.userId = :userId")
    List<OnboardStudyGoal> findByUserId(@Param("userId") UUID userId);
}