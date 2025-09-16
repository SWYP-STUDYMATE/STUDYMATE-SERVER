package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.OnboardLangLevel;
import com.studymate.domain.onboard.entity.OnboardLangLevelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OnboardLangLevelRepository extends JpaRepository<OnboardLangLevel, OnboardLangLevelId> {
    
    /**
     * 사용자 ID로 언어 레벨 정보 조회
     */
    @Query("SELECT oll FROM OnboardLangLevel oll WHERE oll.id.userId = :userId")
    List<OnboardLangLevel> findByUsrId(@Param("userId") UUID userId);
    
    /**
     * 사용자 ID로 언어 레벨 정보 삭제
     */
    @Query("DELETE FROM OnboardLangLevel oll WHERE oll.id.userId = :userId")
    void deleteByUsrId(@Param("userId") UUID userId);
}
