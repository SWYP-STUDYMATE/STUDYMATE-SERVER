package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OnboardScheduleRepository extends JpaRepository<OnboardSchedule, UUID> {
}
