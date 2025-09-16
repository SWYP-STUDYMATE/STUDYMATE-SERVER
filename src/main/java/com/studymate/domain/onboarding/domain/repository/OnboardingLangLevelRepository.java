package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardingLangLevel;
import com.studymate.domain.onboarding.entity.OnboardingLangLevelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardingLangLevelRepository extends JpaRepository<OnboardingLangLevel, OnboardingLangLevelId> {
    
    /**
     * 사용자 ID로 언어 레벨 정보 조회
     */
    @Query("SELECT oll FROM OnboardingLangLevel oll WHERE oll.id.userId = :userId")
    List<OnboardingLangLevel> findByUsrId(@Param("userId") UUID userId);
    
    /**
     * 사용자 ID로 언어 레벨 정보 삭제
     */
    @Query("DELETE FROM OnboardingLangLevel oll WHERE oll.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
