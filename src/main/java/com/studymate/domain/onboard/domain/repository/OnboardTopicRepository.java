package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardTopic;
import com.studymate.domain.onboard.entity.OnboardTopicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardTopicRepository extends JpaRepository<OnboardTopic, OnboardTopicId> {
    
    @Query("SELECT ot FROM OnboardTopic ot WHERE ot.id.userId = :userId")
    List<OnboardTopic> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardTopic ot WHERE ot.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
