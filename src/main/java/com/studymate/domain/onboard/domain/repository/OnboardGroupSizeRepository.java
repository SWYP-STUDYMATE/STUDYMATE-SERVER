package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardGroupSize;
import com.studymate.domain.onboard.entity.OnboardGroupSizeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardGroupSizeRepository extends JpaRepository <OnboardGroupSize, OnboardGroupSizeId> {
    
    @Query("SELECT ogs FROM OnboardGroupSize ogs WHERE ogs.id.userId = :userId")
    List<OnboardGroupSize> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardGroupSize ogs WHERE ogs.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
