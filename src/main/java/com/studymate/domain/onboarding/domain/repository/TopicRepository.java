package com.studymate.domain.onboarding.domain.repository;

import com.studymate.domain.onboarding.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic,Integer> {
}
