package com.studymate.domain.matching.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studymate.domain.matching.domain.dto.request.AdvancedMatchingFilterRequest;
import com.studymate.domain.user.entity.QUser;
import com.studymate.domain.user.entity.QUserStatus;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatchingRepositoryImpl implements MatchingRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private final QUser user = QUser.user;
    private final QUserStatus userStatus = QUserStatus.userStatus;

    @Override
    public Page<User> findPotentialPartnersWithFilters(UUID currentUserId, 
                                                       AdvancedMatchingFilterRequest filters, 
                                                       Pageable pageable) {
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 기본 조건: 자기 자신 제외, 온보딩 완료, 활성 계정
        builder.and(user.userId.ne(currentUserId))
               .and(user.isOnboardingCompleted.isTrue())
               .and(user.userDisable.isFalse());

        // 필터 조건들 추가
        addBasicFilters(builder, filters);
        addLocationFilters(builder, filters);
        addPersonalityFilters(builder, filters);
        addTopicFilters(builder, filters);
        addStudyGoalFilters(builder, filters);
        addScheduleFilters(builder, filters);
        addActivityFilters(builder, filters);
        addOnlineStatusFilters(builder, filters);

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(filters);

        // 쿼리 실행
        List<User> users = queryFactory
                .selectFrom(user)
                .leftJoin(user.userStatus, userStatus).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회 (성능을 위해 별도 쿼리)
        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public Page<User> findCompatiblePartners(UUID currentUserId, 
                                           double minCompatibilityScore, 
                                           Pageable pageable) {
        // 호환성 점수는 실시간 계산이 필요하므로 기본 쿼리만 제공
        // 실제 호환성 점수는 서비스 레이어에서 필터링
        
        List<User> users = queryFactory
                .selectFrom(user)
                .leftJoin(user.userStatus, userStatus).fetchJoin()
                .where(user.userId.ne(currentUserId)
                      .and(user.isOnboardingCompleted.isTrue())
                      .and(user.userDisable.isFalse()))
                .orderBy(user.userCreatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() * 3) // 호환성 필터링을 위해 더 많이 조회
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(user.userId.ne(currentUserId)
                      .and(user.isOnboardingCompleted.isTrue())
                      .and(user.userDisable.isFalse()))
                .fetchCount();

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public Page<User> findOnlinePartners(UUID currentUserId, 
                                        AdvancedMatchingFilterRequest filters, 
                                        Pageable pageable) {
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 기본 조건
        builder.and(user.userId.ne(currentUserId))
               .and(user.isOnboardingCompleted.isTrue())
               .and(user.userDisable.isFalse())
               .and(userStatus.status.in(UserStatus.OnlineStatus.ONLINE, UserStatus.OnlineStatus.STUDYING));

        // 추가 필터 적용
        if (filters != null) {
            addBasicFilters(builder, filters);
            addLocationFilters(builder, filters);
        }

        List<User> users = queryFactory
                .selectFrom(user)
                .join(user.userStatus, userStatus).fetchJoin()
                .where(builder)
                .orderBy(userStatus.lastSeenAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .join(user.userStatus, userStatus)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(users, pageable, total);
    }

    // Private helper methods

    private void addBasicFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        if (filters.getGender() != null && !"ANY".equals(filters.getGender())) {
            builder.and(user.userGenderType.stringValue().eq(filters.getGender()));
        }

        if (filters.hasAgeFilter()) {
            if (filters.getMinAge() != null) {
                int maxBirthYear = LocalDateTime.now().getYear() - filters.getMinAge();
                builder.and(user.birthyear.loe(String.valueOf(maxBirthYear)));
            }
            if (filters.getMaxAge() != null) {
                int minBirthYear = LocalDateTime.now().getYear() - filters.getMaxAge();
                builder.and(user.birthyear.goe(String.valueOf(minBirthYear)));
            }
        }

        // TODO: nativeLanguage 필드 확인 후 구현
        // if (filters.getNativeLanguage() != null) {
        //     builder.and(user.nativeLanguage.name.eq(filters.getNativeLanguage()));
        // }
    }

    private void addLocationFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        // TODO: location 필드 확인 후 구현
        // if (filters.hasLocationFilter()) {
        //     if (filters.getCountry() != null) {
        //         builder.and(user.location.country.eq(filters.getCountry()));
        //     }
        //     if (filters.getCity() != null) {
        //         builder.and(user.location.city.eq(filters.getCity()));
        //     }
        //     if (filters.getCities() != null && !filters.getCities().isEmpty()) {
        //         builder.and(user.location.city.in(filters.getCities()));
        //     }
        // }
    }

    private void addPersonalityFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        // TODO: QOnboardPersonality 엔티티 필드 확인 후 구현
        // if (filters.hasPersonalityFilter()) {
        //     builder.and(user.userId.in(
        //         queryFactory
        //             .select(onboardingPersonality.userId)
        //             .from(onboardingPersonality)
        //             .where(onboardingPersonality.personalityType.in(filters.getPersonalities()))
        //             .groupBy(onboardingPersonality.userId)
        //             .having(onboardingPersonality.userId.count().goe(filters.getPersonalities().size()))
        //     ));
        // }
    }

    private void addTopicFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        // TODO: QOnboardTopic 엔티티 필드 확인 후 구현
        // if (filters.hasTopicFilter()) {
        //     builder.and(user.userId.in(
        //         queryFactory
        //             .select(onboardTopic.usrId)
        //             .from(onboardTopic)
        //             .where(onboardTopic.topicName.in(filters.getTopics()))
        //             .groupBy(onboardTopic.usrId)
        //             .having(onboardTopic.usrId.count().goe(1)) // 최소 1개 일치
        //     ));
        // }
    }

    private void addStudyGoalFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        // TODO: QOnboardStudyGoal 엔티티 필드 확인 후 구현
        // if (filters.hasStudyGoalFilter()) {
        //     builder.and(user.userId.in(
        //         queryFactory
        //             .select(onboardingStudyGoal.userId)
        //             .from(onboardingStudyGoal)
        //             .where(onboardingStudyGoal.goalType.in(filters.getStudyGoals()))
        //             .groupBy(onboardingStudyGoal.userId)
        //             .having(onboardingStudyGoal.userId.count().goe(1))
        //     ));
        // }
    }

    private void addScheduleFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        // TODO: QOnboardSchedule 엔티티 필드 확인 후 구현
        // if (filters.hasScheduleFilter()) {
        //     if (filters.getAvailableDays() != null && !filters.getAvailableDays().isEmpty()) {
        //         builder.and(user.userId.in(
        //             queryFactory
        //                 .select(onboardSchedule.usrId)
        //                 .from(onboardSchedule)
        //                 .where(onboardSchedule.dayOfWeek.stringValue().in(filters.getAvailableDays()))
        //                 .groupBy(onboardSchedule.usrId)
        //                 .having(onboardSchedule.usrId.count().goe(1))
        //         ));
        //     }
        // }
    }

    private void addActivityFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        if (filters.getMaxDaysInactive() != null) {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(filters.getMaxDaysInactive());
            builder.and(userStatus.lastSeenAt.goe(cutoffTime));
        }

        if (filters.getMinDaysSinceJoined() != null) {
            LocalDateTime minJoinDate = LocalDateTime.now().minusDays(filters.getMinDaysSinceJoined());
            builder.and(user.userCreatedAt.loe(minJoinDate));
        }

        if (filters.getMaxDaysSinceJoined() != null) {
            LocalDateTime maxJoinDate = LocalDateTime.now().minusDays(filters.getMaxDaysSinceJoined());
            builder.and(user.userCreatedAt.goe(maxJoinDate));
        }
    }

    private void addOnlineStatusFilters(BooleanBuilder builder, AdvancedMatchingFilterRequest filters) {
        if (Boolean.TRUE.equals(filters.getOnlineOnly())) {
            builder.and(userStatus.status.in(UserStatus.OnlineStatus.ONLINE, UserStatus.OnlineStatus.STUDYING));
        }

        if (Boolean.TRUE.equals(filters.getStudyingOnly())) {
            builder.and(userStatus.status.eq(UserStatus.OnlineStatus.STUDYING));
        }
    }

    private OrderSpecifier<?> createOrderSpecifier(AdvancedMatchingFilterRequest filters) {
        String sortBy = filters.getSortBy() != null ? filters.getSortBy() : "compatibility";
        Order order = "asc".equals(filters.getSortDirection()) ? Order.ASC : Order.DESC;

        return switch (sortBy.toLowerCase()) {
            case "lastactive" -> new OrderSpecifier<>(order, userStatus.lastSeenAt);
            case "joindate" -> new OrderSpecifier<>(order, user.userCreatedAt);
            case "name" -> new OrderSpecifier<>(order, user.englishName);
            default -> new OrderSpecifier<>(order, user.userCreatedAt); // 기본값
        };
    }
}