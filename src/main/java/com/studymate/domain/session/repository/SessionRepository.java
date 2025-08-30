package com.studymate.domain.session.repository;

import com.studymate.domain.session.entity.Session;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    
    List<Session> findByHostUser(User hostUser);
    
    @Query("SELECT s FROM Session s WHERE s.scheduledStartTime > :now ORDER BY s.scheduledStartTime")
    List<Session> findUpcomingSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Session s WHERE s.targetLanguage = :language AND s.scheduledStartTime > :now")
    List<Session> findByTargetLanguageAndScheduledStartTimeAfter(@Param("language") String language, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Session s WHERE s.sessionType = :type AND s.scheduledStartTime > :now")
    List<Session> findBySessionTypeAndScheduledStartTimeAfter(@Param("type") String type, @Param("now") LocalDateTime now);
}