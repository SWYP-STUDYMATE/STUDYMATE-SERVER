package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.PartnerPersonality;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerPersonalityRepository extends JpaRepository<PartnerPersonality, Integer> {
}
