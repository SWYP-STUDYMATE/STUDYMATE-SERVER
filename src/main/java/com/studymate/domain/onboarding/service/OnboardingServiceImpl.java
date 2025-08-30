package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.onboarding.domain.repository.*;
import com.studymate.domain.onboarding.entity.*;
import com.studymate.domain.user.domain.dto.response.OnboardingStatusResponse;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LangLevelTypeRepository langLevelTypeRepository;
    private final MotivationRepository motivationRepository;
    private final TopicRepository topicRepository;
    private final LearningStyleRepository learningStyleRepository;
    private final PartnerPersonalityRepository partnerPersonalityRepository;
    private final GroupSizeRepository groupSizeRepository;
    private final ScheduleRepository scheduleRepository;
    
    // 온보딩 관련 Repository들
    private final OnboardLangLevelRepository onboardLangLevelRepository;
    private final OnboardMotivationRepository onboardMotivationRepository;
    private final OnboardTopicRepository onboardTopicRepository;
    private final OnboardLearningStyleRepository onboardLearningStyleRepository;
    private final OnboardPartnerRepository onboardPartnerRepository;
    private final OnboardGroupSizeRepository onboardGroupSizeRepository;
    private final OnboardScheduleRepository onboardScheduleRepository;

    @Override
    public OnboardingDataResponse getOnboardingData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 사용자의 현재 온보딩 데이터 수집
        OnboardingDataResponse.UserOnboardingData userOnboardingData = new OnboardingDataResponse.UserOnboardingData();
        
        // 언어 정보 설정
        if (user.getNativeLanguage() != null) {
            userOnboardingData.setNativeLanguageId(user.getNativeLanguage().getLanguageId());
        }
        
        // 목표 언어들 설정
        List<OnboardLangLevel> userLanguageLevels = onboardLangLevelRepository.findByUsrId(userId);
        List<OnboardingDataResponse.UserOnboardingData.SelectedTargetLanguage> targetLanguages = 
            userLanguageLevels.stream().map(oll -> {
                OnboardingDataResponse.UserOnboardingData.SelectedTargetLanguage targetLang = 
                    new OnboardingDataResponse.UserOnboardingData.SelectedTargetLanguage();
                targetLang.setLanguageId(oll.getLanguageId());
                targetLang.setLanguageName(oll.getLanguage().getName());
                targetLang.setCurrentLevelId(oll.getCurrentLevel().getLangLevelTypeId());
                targetLang.setCurrentLevelName(oll.getCurrentLevel().getName());
                targetLang.setTargetLevelId(oll.getTargetLevel().getLangLevelTypeId());
                targetLang.setTargetLevelName(oll.getTargetLevel().getName());
                return targetLang;
            }).toList();
        userOnboardingData.setTargetLanguages(targetLanguages);
        
        // 관심사 정보들 설정
        userOnboardingData.setMotivationIds(
            onboardMotivationRepository.findByUsrId(userId).stream()
                .map(om -> om.getMotivationId()).toList());
        
        userOnboardingData.setTopicIds(
            onboardTopicRepository.findByUsrId(userId).stream()
                .map(ot -> ot.getTopicId()).toList());
        
        userOnboardingData.setLearningStyleIds(
            onboardLearningStyleRepository.findByUsrId(userId).stream()
                .map(ols -> ols.getLearningStyleId()).toList());
        
        // 파트너 선호도 설정
        userOnboardingData.setPartnerPersonalityIds(
            onboardPartnerRepository.findByUsrId(userId).stream()
                .map(op -> op.getPartnerPersonalityId()).toList());
        
        userOnboardingData.setGroupSizeIds(
            onboardGroupSizeRepository.findByUsrId(userId).stream()
                .map(ogs -> ogs.getGroupSizeId()).toList());
        
        // 스케줄 정보 설정
        userOnboardingData.setScheduleIds(
            onboardScheduleRepository.findByUsrId(userId).stream()
                .map(os -> os.getScheduleId()).toList());

        // 사용 가능한 옵션들 수집
        OnboardingDataResponse.OnboardingOptions availableOptions = new OnboardingDataResponse.OnboardingOptions();
        
        // 언어 옵션들
        List<OnboardingDataResponse.OnboardingOptions.LanguageOption> languages = 
            languageRepository.findAll().stream().map(lang -> 
                new OnboardingDataResponse.OnboardingOptions.LanguageOption(
                    lang.getLanguageId(), lang.getName(), lang.getCode())).toList();
        availableOptions.setLanguages(languages);
        
        // 레벨 옵션들
        List<OnboardingDataResponse.OnboardingOptions.LevelOption> levels = 
            langLevelTypeRepository.findAll().stream().map(level -> 
                new OnboardingDataResponse.OnboardingOptions.LevelOption(
                    level.getLangLevelTypeId(), level.getName(), level.getDescription(), level.getCategory())).toList();
        availableOptions.setLevels(levels);
        
        // 동기 옵션들
        List<OnboardingDataResponse.OnboardingOptions.MotivationOption> motivations = 
            motivationRepository.findAll().stream().map(motivation -> 
                new OnboardingDataResponse.OnboardingOptions.MotivationOption(
                    motivation.getMotivationId(), motivation.getName(), motivation.getDescription())).toList();
        availableOptions.setMotivations(motivations);
        
        // 주제 옵션들
        List<OnboardingDataResponse.OnboardingOptions.TopicOption> topics = 
            topicRepository.findAll().stream().map(topic -> 
                new OnboardingDataResponse.OnboardingOptions.TopicOption(
                    topic.getTopicId(), topic.getName(), topic.getDescription())).toList();
        availableOptions.setTopics(topics);
        
        // 학습 스타일 옵션들
        List<OnboardingDataResponse.OnboardingOptions.LearningStyleOption> learningStyles = 
            learningStyleRepository.findAll().stream().map(style -> 
                new OnboardingDataResponse.OnboardingOptions.LearningStyleOption(
                    style.getLearningStyleId(), style.getName(), style.getDescription())).toList();
        availableOptions.setLearningStyles(learningStyles);
        
        // 파트너 성격 옵션들
        List<OnboardingDataResponse.OnboardingOptions.PartnerPersonalityOption> partnerPersonalities = 
            partnerPersonalityRepository.findAll().stream().map(personality -> 
                new OnboardingDataResponse.OnboardingOptions.PartnerPersonalityOption(
                    personality.getPartnerPersonalityId(), personality.getName(), personality.getDescription())).toList();
        availableOptions.setPartnerPersonalities(partnerPersonalities);
        
        // 그룹 크기 옵션들
        List<OnboardingDataResponse.OnboardingOptions.GroupSizeOption> groupSizes = 
            groupSizeRepository.findAll().stream().map(size -> 
                new OnboardingDataResponse.OnboardingOptions.GroupSizeOption(
                    size.getGroupSizeId(), size.getName(), size.getDescription())).toList();
        availableOptions.setGroupSizes(groupSizes);
        
        // 스케줄 옵션들
        List<OnboardingDataResponse.OnboardingOptions.ScheduleOption> schedules = 
            scheduleRepository.findAll().stream().map(schedule -> 
                new OnboardingDataResponse.OnboardingOptions.ScheduleOption(
                    schedule.getScheduleId(), schedule.getName(), schedule.getTimeSlot(), schedule.getDayOfWeek())).toList();
        availableOptions.setSchedules(schedules);

        return new OnboardingDataResponse(userOnboardingData, availableOptions);
    }

    @Override
    public void completeAllOnboarding(UUID userId, CompleteAllOnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 기존 온보딩 데이터 삭제
        clearExistingOnboardingData(userId);
        
        // 새로운 온보딩 데이터 저장
        saveOnboardingData(userId, request);
        
        // 온보딩 완료 처리
        user.setIsOnboardingCompleted(true);
        userRepository.save(user);
    }

    @Override
    public OnboardingStatusResponse getOnboardingProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 기본 정보 완성도 체크
        boolean basicInfoCompleted = user.getEnglishName() != null && 
                                   user.getBirthyear() != null && 
                                   user.getGender() != null;

        // 언어 정보 완성도
        boolean languageInfoCompleted = user.getNativeLanguage() != null && 
                                      !onboardLangLevelRepository.findByUsrId(userId).isEmpty();

        // 관심사 정보 완성도
        boolean interestInfoCompleted = !onboardMotivationRepository.findByUsrId(userId).isEmpty() && 
                                      !onboardTopicRepository.findByUsrId(userId).isEmpty();

        // 파트너 선호도 완성도
        boolean partnerInfoCompleted = !onboardPartnerRepository.findByUsrId(userId).isEmpty();

        // 스케줄 정보 완성도
        boolean scheduleInfoCompleted = !onboardScheduleRepository.findByUsrId(userId).isEmpty();

        int completedSteps = 0;
        if (basicInfoCompleted) completedSteps++;
        if (languageInfoCompleted) completedSteps++;
        if (interestInfoCompleted) completedSteps++;
        if (partnerInfoCompleted) completedSteps++;
        if (scheduleInfoCompleted) completedSteps++;

        return new OnboardingStatusResponse(
                basicInfoCompleted,
                languageInfoCompleted,
                interestInfoCompleted,
                partnerInfoCompleted,
                scheduleInfoCompleted,
                user.getIsOnboardingCompleted(),
                completedSteps,
                5
        );
    }

    private void clearExistingOnboardingData(UUID userId) {
        // 기존 데이터 삭제
        onboardLangLevelRepository.deleteByUsrId(userId);
        onboardMotivationRepository.deleteByUsrId(userId);
        onboardTopicRepository.deleteByUsrId(userId);
        onboardLearningStyleRepository.deleteByUsrId(userId);
        onboardPartnerRepository.deleteByUsrId(userId);
        onboardGroupSizeRepository.deleteByUsrId(userId);
        onboardScheduleRepository.deleteByUsrId(userId);
    }

    private void saveOnboardingData(UUID userId, CompleteAllOnboardingRequest request) {
        // TODO: 실제 온보딩 데이터를 각 테이블에 저장하는 로직 구현
        // 1. 언어 레벨 데이터 저장
        // 2. 관심사 데이터 저장  
        // 3. 파트너 선호도 저장
        // 4. 스케줄 정보 저장
        
        // 복합키 구조로 되어있어서 실제 구현시 복잡함
        // 현재는 기본 구조만 제공
    }
}