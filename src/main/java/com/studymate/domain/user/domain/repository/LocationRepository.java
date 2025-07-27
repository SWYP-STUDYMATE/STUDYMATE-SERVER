package com.studymate.domain.user.domain.repository;

import com.studymate.domain.user.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location,Integer> {
}
