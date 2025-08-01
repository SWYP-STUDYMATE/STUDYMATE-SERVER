package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatImageRepository extends JpaRepository<ChatImage, Long> {
}
