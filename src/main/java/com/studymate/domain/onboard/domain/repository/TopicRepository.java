package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic,Integer> {
}
