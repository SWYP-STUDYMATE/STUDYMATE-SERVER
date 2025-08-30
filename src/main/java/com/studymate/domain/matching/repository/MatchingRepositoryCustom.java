package com.studymate.domain.matching.repository;

import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MatchingRepositoryCustom {
    
    /**
     * 고급 필터를 사용한 최적화된 파트너 검색
     */
    Page<User> findPotentialPartnersWithFilters(UUID currentUserId, 
                                               AdvancedMatchingFilterRequest filters, 
                                               Pageable pageable);
    
    /**
     * 호환성 기반 파트너 검색 (캐시된 호환성 점수 활용)
     */
    Page<User> findCompatiblePartners(UUID currentUserId, 
                                     double minCompatibilityScore, 
                                     Pageable pageable);
    
    /**
     * 온라인 사용자 중에서 파트너 검색
     */
    Page<User> findOnlinePartners(UUID currentUserId, 
                                 AdvancedMatchingFilterRequest filters, 
                                 Pageable pageable);
}