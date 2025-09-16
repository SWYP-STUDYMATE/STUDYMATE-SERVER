package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardPartner;
import com.studymate.domain.onboard.entity.OnboardPartnerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardPartnerRepository extends JpaRepository<OnboardPartner, OnboardPartnerId> {
    
    @Query("SELECT op FROM OnboardPartner op WHERE op.id.userId = :userId")
    List<OnboardPartner> findByUsrId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM OnboardPartner op WHERE op.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
