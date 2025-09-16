package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.PartnerPersonality;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerPersonalityRepository extends JpaRepository<PartnerPersonality, Integer> {
}
