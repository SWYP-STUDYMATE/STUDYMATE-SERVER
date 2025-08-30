package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardLearningStyle;
import com.studymate.domain.onboarding.entity.OnboardLearningStyleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardLearningStyleRepository extends JpaRepository<OnboardLearningStyle, OnboardLearningStyleId> {
    
    @Query("SELECT ols FROM OnboardLearningStyle ols WHERE ols.id.userId = :userId")
    List<OnboardLearningStyle> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardLearningStyle ols WHERE ols.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
