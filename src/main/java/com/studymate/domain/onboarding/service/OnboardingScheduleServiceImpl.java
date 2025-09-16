package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.*;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.dto.response.GroupSizeResponse;
import com.studymate.domain.onboarding.domain.repository.GroupSizeRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingGroupSizeRepository;
import com.studymate.domain.onboarding.domain.repository.OnboardingScheduleRepository;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.entity.OnboardingGroupSize;
import com.studymate.domain.onboarding.entity.OnboardingGroupSizeId;
import com.studymate.domain.onboarding.entity.OnboardingSchedule;
import com.studymate.domain.onboarding.entity.OnboardingScheduleId;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OnboardingScheduleServiceImpl implements OnboardingScheduleService{

    private final OnboardingScheduleRepository onboardingScheduleRepository;
    private final UserRepository userRepository;
    private final OnboardingGroupSizeRepository onboardingGroupSizeRepository;
    private final GroupSizeRepository groupSizeRepository;

    @Override
    public void saveOnboardingSchedules(UUID userId,OnboardingScheduleRequests req) {

        List<OnboardingSchedule> schedules = req.schedules().stream()
                .map(s ->createSchedule(userId,s))
                .collect(Collectors.toList());
        onboardingScheduleRepository.saveAll(schedules);
    }
    private OnboardingSchedule createSchedule (UUID userId,OnboardingScheduleRequest req) {
        OnboardingScheduleId scheduleId = new OnboardingScheduleId();
        scheduleId.setUserId(userId);
        scheduleId.setDayOfWeek(req.dayOfWeek());
        scheduleId.setClassTime(req.classTime());
        return OnboardingSchedule.builder()
                .id(scheduleId)
                .build();
    }

    @Override
    public void saveDailyMinute(UUID userId,DailyMinuteRequest req) {
        DailyMinuteType dailyMinutesType = req.dailyMinutesType();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("USER NOT FOUND"));
        user.setDailyMinuteType(dailyMinutesType);
        userRepository.save(user);
    }

    @Override
    public void saveCommunicationMethod(UUID userId,CommunicationMethodRequest req) {
        CommunicationMethodType communicationMethodType = req.communicationMethodType();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("USER NOT FOUND"));
        user.setCommunicationMethodType(communicationMethodType);
        userRepository.save(user);
    }

    @Override
    public void saveOnboardingGroupSize(UUID userId, GroupSizeRequest req) {
        List<Integer> groupSizeIds = req.groupSizeIds();
        List<OnboardingGroupSize> onboardGroupSeizes = groupSizeIds.stream()
                .map(groupSizeId-> OnboardingGroupSize.builder()
                        .id(new OnboardingGroupSizeId(userId,groupSizeId))
                        .build())
                        .toList();
        onboardingGroupSizeRepository.saveAll(onboardGroupSeizes);

    }

    @Override
    public List<CommunicationMethodResponse> getAllCommunication() {
        return Arrays.stream(CommunicationMethodType.values())
                .map(e -> new CommunicationMethodResponse(e.name(), e.getDescription()))
                .toList();
    }

    @Override
    public List<DailyMinuteResponse> getAllDailyMethod() {
        return Arrays.stream(DailyMinuteType.values())
                .map(e -> new DailyMinuteResponse(e.name(), e.getDescription()))
                .toList();
    }

    @Override
    public List<GroupSizeResponse> getAllGroupSize() {
        return groupSizeRepository.findAll().stream()
                .map(g-> new GroupSizeResponse(
                        g.getGroupSizeId(),
                        g.getGroupSize()
                ))
                .toList();
    }






}
