package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardSchedule;
import com.studymate.domain.onboard.entity.OnboardScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardScheduleRepository extends JpaRepository<OnboardSchedule, OnboardScheduleId> {
    
    @Query("SELECT os FROM OnboardSchedule os WHERE os.id.userId = :userId")
    List<OnboardSchedule> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardSchedule os WHERE os.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
