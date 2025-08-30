package com.studymate.domain.matching.service;

import com.studymate.domain.matching.domain.dto.response.CompatibilityScoreResponse;
import com.studymate.domain.user.entity.User;

public interface CompatibilityCalculatorService {
    CompatibilityScoreResponse calculateCompatibility(User user1, User user2);
    double calculateSimpleScore(User user1, User user2);
}