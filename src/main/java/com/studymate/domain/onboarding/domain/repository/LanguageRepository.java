package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language,Integer> {
}
