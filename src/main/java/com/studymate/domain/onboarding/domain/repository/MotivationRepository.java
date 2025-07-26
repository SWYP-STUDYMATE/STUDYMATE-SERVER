package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboarding.entity.Motivation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivationRepository extends JpaRepository<Motivation,Integer> {
}
