package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.domain.dto.response.MotivationResponse;
import com.studymate.domain.onboard.entity.Motivation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivationRepository extends JpaRepository<Motivation,Integer> {
}
