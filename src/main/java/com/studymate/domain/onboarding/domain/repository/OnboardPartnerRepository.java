package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.OnboardPartner;
import com.studymate.domain.onboarding.entity.OnboardPartnerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardPartnerRepository extends JpaRepository<OnboardPartner, OnboardPartnerId> {
}
