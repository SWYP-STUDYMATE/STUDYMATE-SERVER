package com.studymate.domain.onboard.domain.repository;

import com.studymate.domain.onboard.domain.type.DayOfWeekType;
import com.studymate.domain.onboard.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Schedule 엔티티에 대한 Repository
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    /**
     * 요일별 스케줄 조회
     */
    List<Schedule> findByDayOfWeekType(DayOfWeekType dayOfWeekType);

    /**
     * 모든 활성 스케줄 조회
     */
    List<Schedule> findAllByOrderByScheduleIdAsc();
}