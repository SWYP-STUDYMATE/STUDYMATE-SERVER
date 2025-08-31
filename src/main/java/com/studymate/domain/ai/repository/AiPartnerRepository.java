package com.studymate.domain.ai.repository;

import com.studymate.domain.ai.entity.AiPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiPartnerRepository extends JpaRepository<AiPartner, UUID> {
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.isActive = true")
    List<AiPartner> findAllActive();
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.targetLanguage = :targetLanguage AND ap.languageLevel = :languageLevel AND ap.isActive = true")
    List<AiPartner> findByLanguageAndLevel(
        @Param("targetLanguage") String targetLanguage,
        @Param("languageLevel") String languageLevel
    );
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.specialty = :specialty AND ap.isActive = true")
    List<AiPartner> findBySpecialty(@Param("specialty") String specialty);
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.personalityType = :personalityType AND ap.isActive = true")
    List<AiPartner> findByPersonalityType(@Param("personalityType") String personalityType);
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.isActive = true ORDER BY ap.ratingAverage DESC, ap.sessionCount DESC")
    List<AiPartner> findTopRatedPartners();
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.isActive = true ORDER BY ap.sessionCount DESC")
    List<AiPartner> findMostPopularPartners();
    
    @Query("SELECT ap FROM AiPartner ap WHERE ap.name LIKE %:keyword% OR ap.description LIKE %:keyword% AND ap.isActive = true")
    List<AiPartner> searchByKeyword(@Param("keyword") String keyword);
}