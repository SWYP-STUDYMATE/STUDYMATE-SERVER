package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CommunicationMethodRequest;
import com.studymate.domain.onboarding.domain.dto.request.DailyMinuteRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequests;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.repository.OnboardScheduleRepository;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.entity.OnboardSchedule;
import com.studymate.domain.onboarding.entity.OnboardScheduleId;
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
public class OnboardScheduleServiceImpl implements OnboardScheduleService{

    private final OnboardScheduleRepository onboardScheduleRepository;
    private final UserRepository userRepository;

    @Override
    public void saveOnboardSchedules(UUID userId,OnboardScheduleRequests req) {

        List<OnboardSchedule> schedules = req.schedules().stream()
                .map(s ->createSchedule(userId,s))
                .collect(Collectors.toList());
        onboardScheduleRepository.saveAll(schedules);
    }
    private OnboardSchedule createSchedule (UUID userId,OnboardScheduleRequest req) {
        OnboardScheduleId scheduleId = new OnboardScheduleId();
        scheduleId.setUserId(userId);
        scheduleId.setDayOfWeek(req.dayOfWeek());
        scheduleId.setClassTime(req.classTime());
        return OnboardSchedule.builder()
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
        System.out.println("saved user: " + user.getCommunicationMethodType());



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






}
