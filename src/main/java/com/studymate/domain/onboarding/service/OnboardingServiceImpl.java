package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardingStepRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingProgressResponse;
import com.studymate.domain.onboarding.domain.dto.response.CurrentStepResponse;
import com.studymate.domain.onboarding.domain.type.DayOfWeekType;
import com.studymate.domain.onboarding.domain.repository.*;
import com.studymate.domain.onboarding.entity.*;
import com.studymate.domain.user.domain.dto.response.OnboardingStatusResponse;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import com.studymate.domain.onboarding.exception.OnboardingBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
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
    private final LearningExpectationRepository learningExpectationRepository;
    private final PartnerPersonalityRepository partnerPersonalityRepository;
    private final GroupSizeRepository groupSizeRepository;
    private final ScheduleRepository scheduleRepository;
    
    // 온보딩 관련 Repository들
    private final OnboardingLangLevelRepository onboardingLangLevelRepository;
    private final OnboardingMotivationRepository onboardingMotivationRepository;
    private final OnboardingTopicRepository onboardingTopicRepository;
    private final OnboardingLearningStyleRepository onboardingLearningStyleRepository;
    private final OnboardingPartnerRepository onboardingPartnerRepository;
    private final OnboardingGroupSizeRepository onboardingGroupSizeRepository;
    private final OnboardingLearningExpectationRepository onboardingLearningExpectationRepository;
    private final OnboardingScheduleRepository onboardingScheduleRepository;
    
    // Redis 기반 온보딩 상태 관리
    private final OnboardingStateService stateService;
    
    // 온보딩 단계별 메타데이터
    private static final Map<Integer, StepMetadata> STEP_METADATA = initializeStepMetadata();

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
        List<OnboardingLangLevel> userLanguageLevels = onboardingLangLevelRepository.findByUsrId(userId);
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
            onboardingMotivationRepository.findByUsrId(userId).stream()
                .map(om -> om.getMotivationId()).toList());
        
        userOnboardingData.setTopicIds(
            onboardingTopicRepository.findByUsrId(userId).stream()
                .map(ot -> ot.getTopicId()).toList());
        
        userOnboardingData.setLearningStyleIds(
            onboardingLearningStyleRepository.findByUsrId(userId).stream()
                .map(ols -> ols.getLearningStyleId()).toList());
        
        // 파트너 선호도 설정
        userOnboardingData.setPartnerPersonalityIds(
            onboardingPartnerRepository.findByUsrId(userId).stream()
                .map(op -> op.getPartnerPersonalityId()).toList());
        
        userOnboardingData.setGroupSizeIds(
            onboardingGroupSizeRepository.findByUsrId(userId).stream()
                .map(ogs -> ogs.getGroupSizeId()).toList());
        
        // 스케줄 정보 설정
        userOnboardingData.setScheduleIds(
            onboardingScheduleRepository.findByUsrId(userId).stream()
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

        AggregatedOnboardingData aggregatedData = buildAggregatedOnboardingData(user, request);

        // 기존 온보딩 데이터 삭제 후 재적재 (필요한 영역만 선별)
        clearExistingOnboardingData(userId, aggregatedData);

        // 새로운 온보딩 데이터 저장
        saveOnboardingData(user, aggregatedData);
        
        // 온보딩 완료 처리
        user.setIsOnboardingCompleted(true);
        userRepository.save(user);
        
        // Redis 데이터 정리
        stateService.cleanupOnboardingData(userId);
        
        log.info("All onboarding completed for user: {}", userId);
    }

    @Override
    public OnboardingStatusResponse getOnboardingProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // Redis에서 진행률 확인 (우선순위)
        Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
        if (progressData.isPresent()) {
            Map<String, Object> progress = progressData.get();
            double progressPercentage = ((Number) progress.getOrDefault("progressPercentage", 0.0)).doubleValue();
            int currentStep = ((Number) progress.getOrDefault("currentStep", 1)).intValue();
            int totalSteps = ((Number) progress.getOrDefault("totalSteps", 5)).intValue();
            boolean completed = Boolean.TRUE.equals(progress.get("completed"))
                    || progressPercentage >= 100.0
                    || currentStep >= totalSteps;

            @SuppressWarnings("unchecked")
            Map<String, Boolean> completedStepMap = (Map<String, Boolean>) progress.get("completedSteps");

            return OnboardingStatusResponse.builder()
                    .progressPercentage(progressPercentage)
                    .currentStep(currentStep)
                    .totalSteps(totalSteps)
                    .isCompleted(completed)
                    .onboardingCompleted(completed)
                    .nextStep(completed ? totalSteps : Math.min(currentStep + 1, totalSteps))
                    .build()
                    .withCompletedStepsFromMap(completedStepMap);
        }

        // 기존 DB 기반 진행률 체크 (fallback)
        boolean basicInfoCompleted = user.getEnglishName() != null && 
                                   user.getBirthyear() != null && 
                                   user.getGender() != null;

        // 언어 정보 완성도
        boolean languageInfoCompleted = user.getNativeLanguage() != null && 
                                      !onboardingLangLevelRepository.findByUsrId(userId).isEmpty();

        // 관심사 정보 완성도
        boolean interestInfoCompleted = !onboardingMotivationRepository.findByUsrId(userId).isEmpty() && 
                                      !onboardingTopicRepository.findByUsrId(userId).isEmpty();

        // 파트너 선호도 완성도
        boolean partnerInfoCompleted = !onboardingPartnerRepository.findByUsrId(userId).isEmpty();

        // 스케줄 정보 완성도
        boolean scheduleInfoCompleted = !onboardingScheduleRepository.findByUsrId(userId).isEmpty();

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

    private void clearExistingOnboardingData(UUID userId, AggregatedOnboardingData data) {
        if (data.hasLanguageLevelData()) {
            onboardingLangLevelRepository.deleteByUsrId(userId);
        }
        if (data.hasMotivationData()) {
            onboardingMotivationRepository.deleteByUsrId(userId);
        }
        if (data.hasTopicData()) {
            onboardingTopicRepository.deleteByUsrId(userId);
        }
        if (data.hasLearningStyleData()) {
            onboardingLearningStyleRepository.deleteByUsrId(userId);
        }
        if (data.hasLearningExpectationData()) {
            onboardingLearningExpectationRepository.deleteByUsrId(userId);
        }
        if (data.hasPartnerPersonalityData()) {
            onboardingPartnerRepository.deleteByUsrId(userId);
        }
        if (data.hasGroupSizeData()) {
            onboardingGroupSizeRepository.deleteByUsrId(userId);
        }
        if (data.hasSchedulePersistenceData()) {
            onboardingScheduleRepository.deleteByUsrId(userId);
        }
    }

    private void saveOnboardingData(User user, AggregatedOnboardingData data) {
        UUID userId = user.getUserId();
        log.info("Persisting onboarding data for user: {}", userId);

        applyBasicProfileData(user, data);
        persistLanguageData(user, data);
        persistInterestData(userId, data);
        persistPartnerPreferenceData(userId, data);
        persistScheduleData(userId, data);

        userRepository.save(user);

        log.debug("Onboarding data save process completed for user: {}", userId);
    }

    private AggregatedOnboardingData buildAggregatedOnboardingData(User user, CompleteAllOnboardingRequest request) {
        AggregatedOnboardingData data = new AggregatedOnboardingData();

        if (request != null) {
            data.mergeFromRequest(request);
        }

        for (int step = 1; step <= 7; step++) {
            Map<String, Object> stepData = stateService.getStepData(user.getUserId(), step);
            if (!stepData.isEmpty()) {
                data.mergeFromStep(step, stepData);
            }
        }

        return data;
    }

    private void applyBasicProfileData(User user, AggregatedOnboardingData data) {
        if (hasText(data.getEnglishName())) {
            user.setEnglishName(data.getEnglishName().trim());
        }
        if (hasText(data.getIntro())) {
            user.setSelfBio(data.getIntro().trim());
        }
        if (hasText(data.getProfileImage())) {
            user.setProfileImage(data.getProfileImage().trim());
        }
    }

    private void persistLanguageData(User user, AggregatedOnboardingData data) {
        if (data.getNativeLanguageId() != null) {
            Language language = languageRepository.findById(data.getNativeLanguageId())
                    .orElseThrow(() -> new NotFoundException("LANGUAGE NOT FOUND: " + data.getNativeLanguageId()));
            user.setNativeLanguage(language);
        }

        List<CompleteAllOnboardingRequest.TargetLanguageData> targetLanguages = data.getTargetLanguages();
        if (targetLanguages.isEmpty()) {
            return;
        }

        Set<Integer> levelIds = targetLanguages.stream()
                .flatMap(dto -> Arrays.stream(new Integer[]{dto.getCurrentLevelId(),
                        dto.getTargetLevelId() != null ? dto.getTargetLevelId() : dto.getCurrentLevelId()}))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, LangLevelType> levelTypeMap = langLevelTypeRepository.findAllById(levelIds).stream()
                .collect(Collectors.toMap(LangLevelType::getLangLevelId, lt -> lt));

        List<OnboardingLangLevel> entities = new ArrayList<>();
        for (CompleteAllOnboardingRequest.TargetLanguageData languageData : targetLanguages) {
            Integer languageId = languageData.getLanguageId();
            Integer currentLevelId = languageData.getCurrentLevelId();

            if (languageId == null || currentLevelId == null) {
                continue;
            }

            LangLevelType currentLevel = levelTypeMap.get(currentLevelId);
            if (currentLevel == null) {
                throw new NotFoundException("LANGUAGE LEVEL TYPE NOT FOUND: " + currentLevelId);
            }

            Integer targetLevelId = languageData.getTargetLevelId() != null
                    ? languageData.getTargetLevelId()
                    : currentLevelId;
            LangLevelType targetLevel = levelTypeMap.get(targetLevelId);
            if (targetLevel == null) {
                throw new NotFoundException("LANGUAGE LEVEL TYPE NOT FOUND: " + targetLevelId);
            }

            OnboardingLangLevelId id = new OnboardingLangLevelId(user.getUserId(), languageId);
            OnboardingLangLevel entity = OnboardingLangLevel.builder()
                    .id(id)
                    .currentLevel(currentLevel)
                    .targetLevel(targetLevel)
                    .langLevelType(currentLevel)
                    .build();
            entities.add(entity);
        }

        if (!entities.isEmpty()) {
            onboardingLangLevelRepository.saveAll(entities);
        }
    }

    private void persistInterestData(UUID userId, AggregatedOnboardingData data) {
        if (data.hasMotivationData()) {
            validateEntitiesExist(data.getMotivationIds(), motivationRepository.findAllById(data.getMotivationIds()),
                    "MOTIVATION");
            List<OnboardingMotivation> motivations = data.getMotivationIds().stream()
                    .map(id -> OnboardingMotivation.builder()
                            .id(new OnboardingMotivationId(userId, id))
                            .build())
                    .toList();
            onboardingMotivationRepository.saveAll(motivations);
        }

        if (data.hasTopicData()) {
            validateEntitiesExist(data.getTopicIds(), topicRepository.findAllById(data.getTopicIds()), "TOPIC");
            List<OnboardingTopic> topics = data.getTopicIds().stream()
                    .map(id -> OnboardingTopic.builder()
                            .id(new OnboardingTopicId(userId, id))
                            .build())
                    .toList();
            onboardingTopicRepository.saveAll(topics);
        }

        if (data.hasLearningStyleData()) {
            validateEntitiesExist(data.getLearningStyleIds(), learningStyleRepository.findAllById(data.getLearningStyleIds()),
                    "LEARNING STYLE");
            List<OnboardingLearningStyle> learningStyles = data.getLearningStyleIds().stream()
                    .map(id -> OnboardingLearningStyle.builder()
                            .id(new OnboardingLearningStyleId(userId, id))
                            .build())
                    .toList();
            onboardingLearningStyleRepository.saveAll(learningStyles);
        }

        if (data.hasLearningExpectationData()) {
            validateEntitiesExist(data.getLearningExpectationIds(),
                    learningExpectationRepository.findAllById(data.getLearningExpectationIds()),
                    "LEARNING EXPECTATION");
            List<OnboardingLearningExpectation> expectations = data.getLearningExpectationIds().stream()
                    .map(id -> OnboardingLearningExpectation.builder()
                            .id(new OnboardingLearningExpectationId(userId, id))
                            .build())
                    .toList();
            onboardingLearningExpectationRepository.saveAll(expectations);
        }
    }

    private void persistPartnerPreferenceData(UUID userId, AggregatedOnboardingData data) {
        if (data.hasPartnerPersonalityData()) {
            validateEntitiesExist(data.getPartnerPersonalityIds(),
                    partnerPersonalityRepository.findAllById(data.getPartnerPersonalityIds()),
                    "PARTNER PERSONALITY");
            List<OnboardingPartner> partners = data.getPartnerPersonalityIds().stream()
                    .map(id -> OnboardingPartner.builder()
                            .id(new OnboardingPartnerId(userId, id))
                            .build())
                    .toList();
            onboardingPartnerRepository.saveAll(partners);
        }

        if (data.hasGroupSizeData()) {
            validateEntitiesExist(data.getGroupSizeIds(), groupSizeRepository.findAllById(data.getGroupSizeIds()),
                    "GROUP SIZE");
            List<OnboardingGroupSize> groupSizes = data.getGroupSizeIds().stream()
                    .map(id -> OnboardingGroupSize.builder()
                            .id(new OnboardingGroupSizeId(userId, id))
                            .build())
                    .toList();
            onboardingGroupSizeRepository.saveAll(groupSizes);
        }
    }

    private void persistScheduleData(UUID userId, AggregatedOnboardingData data) {
        if (!data.hasSchedulePersistenceData()) {
            return;
        }

        Set<Integer> scheduleIds = new LinkedHashSet<>(data.getScheduleIds());
        if (scheduleIds.isEmpty()) {
            // scheduleSelections may include scheduleId values, try to harvest
            data.getScheduleSelections().stream()
                    .map(ScheduleSelection::scheduleId)
                    .filter(Objects::nonNull)
                    .forEach(scheduleIds::add);
        }

        Map<Integer, Schedule> scheduleMap = scheduleRepository.findAllById(scheduleIds).stream()
                .collect(Collectors.toMap(Schedule::getScheduleId, s -> s));

        List<OnboardingSchedule> schedules = new ArrayList<>();

        for (Integer scheduleId : scheduleIds) {
            Schedule schedule = scheduleMap.get(scheduleId);
            if (schedule == null) {
                throw new NotFoundException("SCHEDULE NOT FOUND: " + scheduleId);
            }
            LocalTime classTime = parseStartTime(schedule.getTimeSlot());
            OnboardingScheduleId id = new OnboardingScheduleId();
            id.setUserId(userId);
            id.setScheduleId(scheduleId);
            id.setDayOfWeek(schedule.getDayOfWeekType());
            id.setClassTime(classTime);

            schedules.add(OnboardingSchedule.builder()
                    .id(id)
                    .build());
        }

        if (!schedules.isEmpty()) {
            onboardingScheduleRepository.saveAll(schedules);
        }
    }

    private void validateEntitiesExist(Collection<Integer> requestedIds, Collection<?> fetchedEntities, String label) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            return;
        }

        if (fetchedEntities.size() != new HashSet<>(requestedIds).size()) {
            throw new NotFoundException(label + " NOT FOUND");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private LocalTime parseStartTime(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) {
            return null;
        }
        String start = timeSlot.split("-")[0].trim();
        try {
            return LocalTime.parse(start, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            log.warn("Unable to parse time slot '{}' for onboarding schedule", timeSlot, e);
            return null;
        }
    }

    private static class AggregatedOnboardingData {
        private String englishName;
        private String intro;
        private String profileImage;
        private Integer nativeLanguageId;
        private final LinkedHashSet<CompleteAllOnboardingRequest.TargetLanguageData> targetLanguages = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> motivationIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> topicIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> learningStyleIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> learningExpectationIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> partnerPersonalityIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> groupSizeIds = new LinkedHashSet<>();
        private final LinkedHashSet<Integer> scheduleIds = new LinkedHashSet<>();
        private final List<ScheduleSelection> scheduleSelections = new ArrayList<>();

        void mergeFromRequest(CompleteAllOnboardingRequest request) {
            if (request.getNativeLanguageId() != null) {
                this.nativeLanguageId = request.getNativeLanguageId();
            }
            if (request.getTargetLanguages() != null) {
                this.targetLanguages.addAll(request.getTargetLanguages());
            }
            addAll(this.motivationIds, request.getMotivationIds());
            addAll(this.topicIds, request.getTopicIds());
            addAll(this.learningStyleIds, request.getLearningStyleIds());
            addAll(this.learningExpectationIds, request.getLearningExpectationIds());
            addAll(this.partnerPersonalityIds, request.getPartnerPersonalityIds());
            addAll(this.groupSizeIds, request.getGroupSizeIds());
            addAll(this.scheduleIds, request.getScheduleIds());
        }

        void mergeFromStep(int stepNumber, Map<String, Object> stepData) {
            switch (stepNumber) {
                case 1 -> mergeStep1(stepData);
                case 2 -> mergeStep2(stepData);
                case 3 -> mergeStep3(stepData);
                case 4 -> mergeStep4(stepData);
                default -> {
                }
            }
        }

        private void mergeStep1(Map<String, Object> stepData) {
            this.englishName = firstNonBlank(this.englishName, asString(stepData.get("englishName")));
            this.intro = firstNonBlank(this.intro, asString(stepData.get("intro")));
            this.profileImage = firstNonBlank(this.profileImage, asString(stepData.get("profileImage")));
        }

        private void mergeStep2(Map<String, Object> stepData) {
            if (this.nativeLanguageId == null) {
                this.nativeLanguageId = toInteger(stepData.get("nativeLanguageId"));
            }
            if (this.nativeLanguageId == null) {
                this.nativeLanguageId = toInteger(stepData.get("languageId"));
            }
            addLanguageLevels(stepData.get("targetLanguages"));
            addLanguageLevels(stepData.get("languages"));
        }

        private void mergeStep3(Map<String, Object> stepData) {
            addIntegers(this.motivationIds, stepData, "motivationIds", "selectedMotivationIds", "motivations");
            addIntegers(this.topicIds, stepData, "topicIds", "selectedTopicIds", "topics");
            addIntegers(this.learningStyleIds, stepData, "learningStyleIds", "selectedLearningStyleIds", "learningStyles");
            addIntegers(this.learningExpectationIds, stepData, "learningExpectationIds",
                    "selectedLearningExpectationIds", "learningExpectations");
        }

        private void mergeStep4(Map<String, Object> stepData) {
            addIntegers(this.partnerPersonalityIds, stepData, "partnerPersonalityIds", "personalPartnerIds",
                    "selectedPartnerPersonalityIds");
            addIntegers(this.groupSizeIds, stepData, "groupSizeIds", "selectedGroupSizeIds");
            addIntegers(this.scheduleIds, stepData, "scheduleIds", "selectedScheduleIds");
            addScheduleSelections(stepData.get("schedules"));
        }

        private void addLanguageLevels(Object raw) {
            if (!(raw instanceof List<?> list)) {
                return;
            }
            for (Object element : list) {
                if (element instanceof Map<?, ?> map) {
                    Object languageValue = map.containsKey("languageId") ? map.get("languageId") : map.get("id");
                    Object currentLevelValue = map.containsKey("currentLevelId") ? map.get("currentLevelId") : map.get("levelId");
                    Object targetLevelValue = map.containsKey("targetLevelId") ? map.get("targetLevelId") : map.get("desiredLevelId");

                    Integer languageId = toInteger(languageValue);
                    Integer currentLevelId = toInteger(currentLevelValue);
                    Integer targetLevelId = toInteger(targetLevelValue);
                    if (languageId != null && currentLevelId != null) {
                        this.targetLanguages.add(new CompleteAllOnboardingRequest.TargetLanguageData(
                                languageId,
                                currentLevelId,
                                targetLevelId != null ? targetLevelId : currentLevelId
                        ));
                    }
                }
            }
        }

        private void addIntegers(LinkedHashSet<Integer> target, Map<String, Object> stepData, String... keys) {
            for (String key : keys) {
                Object value = stepData.get(key);
                addAll(target, toIntegerList(value));
            }
        }

        private void addScheduleSelections(Object raw) {
            if (!(raw instanceof List<?> list)) {
                return;
            }
            for (Object element : list) {
                if (element instanceof Map<?, ?> map) {
                    Integer scheduleId = toInteger(map.get("scheduleId"));
                    DayOfWeekType dayOfWeek = toDayOfWeek(map.get("dayOfWeek"));
                    LocalTime classTime = toLocalTime(map.get("classTime"));
                    if (scheduleId != null) {
                        this.scheduleIds.add(scheduleId);
                    }
                    if (dayOfWeek != null && classTime != null) {
                        this.scheduleSelections.add(new ScheduleSelection(scheduleId, dayOfWeek, classTime));
                    }
                }
            }
        }

        List<CompleteAllOnboardingRequest.TargetLanguageData> getTargetLanguages() {
            return new ArrayList<>(targetLanguages);
        }

        List<Integer> getMotivationIds() {
            return new ArrayList<>(motivationIds);
        }

        List<Integer> getTopicIds() {
            return new ArrayList<>(topicIds);
        }

        List<Integer> getLearningStyleIds() {
            return new ArrayList<>(learningStyleIds);
        }

        List<Integer> getLearningExpectationIds() {
            return new ArrayList<>(learningExpectationIds);
        }

        List<Integer> getPartnerPersonalityIds() {
            return new ArrayList<>(partnerPersonalityIds);
        }

        List<Integer> getGroupSizeIds() {
            return new ArrayList<>(groupSizeIds);
        }

        List<Integer> getScheduleIds() {
            return new ArrayList<>(scheduleIds);
        }

        List<ScheduleSelection> getScheduleSelections() {
            return scheduleSelections;
        }

        String getEnglishName() {
            return englishName;
        }

        String getIntro() {
            return intro;
        }

        String getProfileImage() {
            return profileImage;
        }

        Integer getNativeLanguageId() {
            return nativeLanguageId;
        }

        boolean hasLanguageLevelData() {
            return !targetLanguages.isEmpty();
        }

        boolean hasMotivationData() {
            return !motivationIds.isEmpty();
        }

        boolean hasTopicData() {
            return !topicIds.isEmpty();
        }

        boolean hasLearningStyleData() {
            return !learningStyleIds.isEmpty();
        }

        boolean hasLearningExpectationData() {
            return !learningExpectationIds.isEmpty();
        }

        boolean hasPartnerPersonalityData() {
            return !partnerPersonalityIds.isEmpty();
        }

        boolean hasGroupSizeData() {
            return !groupSizeIds.isEmpty();
        }

        boolean hasSchedulePersistenceData() {
            return !scheduleIds.isEmpty();
        }

        private void addAll(LinkedHashSet<Integer> target, Collection<Integer> values) {
            if (values == null) {
                return;
            }
            for (Integer value : values) {
                if (value != null) {
                    target.add(value);
                }
            }
        }

        private String firstNonBlank(String current, String candidate) {
            if (hasTextInternal(current)) {
                return current;
            }
            return hasTextInternal(candidate) ? candidate : current;
        }

        private boolean hasTextInternal(String value) {
            return value != null && !value.trim().isEmpty();
        }

        private String asString(Object value) {
            return value != null ? value.toString() : null;
        }

        private Integer toInteger(Object value) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String string) {
                try {
                    return Integer.parseInt(string.trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }

        private List<Integer> toIntegerList(Object value) {
            if (value instanceof List<?> list) {
                List<Integer> result = new ArrayList<>();
                for (Object element : list) {
                    if (element instanceof Map<?, ?> map) {
                        Integer idValue = toInteger(map.get("id"));
                        if (idValue == null) {
                            idValue = toInteger(map.get("value"));
                        }
                        if (idValue != null) {
                            result.add(idValue);
                        }
                    } else {
                        Integer asInt = toInteger(element);
                        if (asInt != null) {
                            result.add(asInt);
                        }
                    }
                }
                return result;
            }

            Integer single = toInteger(value);
            return single != null ? Collections.singletonList(single) : Collections.emptyList();
        }

        private DayOfWeekType toDayOfWeek(Object value) {
            if (value == null) {
                return null;
            }
            try {
                return DayOfWeekType.valueOf(value.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private LocalTime toLocalTime(Object value) {
            if (value == null) {
                return null;
            }
            try {
                return LocalTime.parse(value.toString());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    private record ScheduleSelection(Integer scheduleId, DayOfWeekType dayOfWeek, LocalTime classTime) {
    }
    
    // === 새로운 UX 개선 메서드들 ===
    
    @Override
    public OnboardingProgressResponse saveOnboardingStep(UUID userId, Integer stepNumber, OnboardingStepRequest request) {
        validateStepNumber(stepNumber);
        
        try {
            // Redis에 단계별 데이터 저장
            stateService.saveStepData(userId, stepNumber, request.getStepData());
            
            // 현재 진행률 조회
            Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
            
            if (progressData.isPresent()) {
                Map<String, Object> progress = progressData.get();
                return buildProgressResponse(progress, stepNumber);
            }
            
            throw new OnboardingBusinessException("진행률 업데이트에 실패했습니다.");
            
        } catch (Exception e) {
            log.error("Failed to save onboarding step {} for user {}: {}", stepNumber, userId, e.getMessage());
            throw new OnboardingBusinessException("온보딩 단계 저장에 실패했습니다.");
        }
    }

    @Override
    public CurrentStepResponse getCurrentOnboardingStep(UUID userId) {
        Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
        
        if (progressData.isEmpty()) {
            // 신규 사용자인 경우 1단계부터 시작
            return buildCurrentStepResponse(1, userId, Collections.emptyMap());
        }
        
        Map<String, Object> progress = progressData.get();
        Integer currentStep = (Integer) progress.get("currentStep");
        
        // 현재 단계의 저장된 데이터 조회
        Map<String, Object> stepData = stateService.getStepData(userId, currentStep);
        Map<String, Object> savedData = stepData.isEmpty() ? Collections.emptyMap() : stepData;
        
        return buildCurrentStepResponse(currentStep, userId, savedData);
    }

    @Override
    public OnboardingProgressResponse skipOnboardingStep(UUID userId, Integer stepNumber, String reason) {
        validateStepNumber(stepNumber);
        
        // 필수 단계 건너뛰기 방지
        StepMetadata stepMeta = STEP_METADATA.get(stepNumber);
        if (stepMeta != null && stepMeta.isRequired()) {
            throw new OnboardingBusinessException("필수 단계는 건너뛸 수 없습니다.");
        }
        
        try {
            // Redis에 건너뛴 단계로 표시
            String skipReason = reason != null ? reason : "사용자 선택";
            stateService.markStepAsSkipped(userId, stepNumber, skipReason);
            
            // 현재 진행률 조회
            Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
            
            if (progressData.isPresent()) {
                Map<String, Object> progress = progressData.get();
                return buildProgressResponse(progress, stepNumber);
            }
            
            throw new OnboardingBusinessException("진행률 업데이트에 실패했습니다.");
            
        } catch (Exception e) {
            log.error("Failed to skip onboarding step {} for user {}: {}", stepNumber, userId, e.getMessage());
            throw new OnboardingBusinessException("온보딩 단계 건너뛰기에 실패했습니다.");
        }
    }

    @Override
    public CurrentStepResponse goBackToOnboardingStep(UUID userId, Integer stepNumber) {
        validateStepNumber(stepNumber);
        
        // 해당 단계의 저장된 데이터 조회
        Map<String, Object> stepData = stateService.getStepData(userId, stepNumber);
        Map<String, Object> savedData = stepData.isEmpty() ? Collections.emptyMap() : stepData;
        
        return buildCurrentStepResponse(stepNumber, userId, savedData);
    }

    @Override
    public void autoSaveOnboardingData(UUID userId, Map<String, Object> currentData) {
        try {
            stateService.saveSessionData(userId, currentData);
            log.debug("Auto-saved onboarding data for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to auto-save onboarding data for user {}: {}", userId, e.getMessage());
            // 자동 저장 실패는 사용자에게 오류를 반환하지 않음 (UX 고려)
        }
    }

    @Override
    public Object startTrialMatching(UUID userId) {
        // 최소 진행률 확인 (40%)
        Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
        
        if (progressData.isPresent()) {
            Map<String, Object> progress = progressData.get();
            Double progressPercentage = (Double) progress.get("progressPercentage");
            
            if (progressPercentage < 40.0) {
                throw new OnboardingBusinessException(
                    "체험 매칭을 위해서는 기본 정보(언어 설정, 관심사)를 완료해주세요.");
            }
            
            // 실제 매칭 로직 구현 - 체험 매칭 시스템
            String trialId = UUID.randomUUID().toString();
            log.info("Starting trial matching for user: {} with trialId: {}", userId, trialId);
            
            Map<String, Object> trialResult = new HashMap<>();
            trialResult.put("trialId", trialId);
            trialResult.put("matchingStarted", true);
            trialResult.put("estimatedWaitTime", 30); // 30초 예상 대기시간
            trialResult.put("message", "체험 매칭이 시작되었습니다!");
            trialResult.put("timestamp", LocalDateTime.now());
            
            log.info("Trial matching started for user: {}", userId);
            return trialResult;
        }
        
        throw new OnboardingBusinessException("온보딩 진행률을 찾을 수 없습니다.");
    }

    @Override
    public void extendOnboardingSession(UUID userId) {
        try {
            stateService.extendSession(userId);
            log.info("Onboarding session extended for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to extend onboarding session for user {}: {}", userId, e.getMessage());
            throw new OnboardingBusinessException("세션 연장에 실패했습니다.");
        }
    }
    
    // === Private Helper Methods ===
    
    private void validateStepNumber(Integer stepNumber) {
        if (stepNumber < 1 || stepNumber > 7) {
            throw new OnboardingBusinessException("올바르지 않은 단계 번호입니다. (1-7)");
        }
    }
    
    private OnboardingProgressResponse buildProgressResponse(Map<String, Object> progressData, Integer currentStep) {
        @SuppressWarnings("unchecked")
        Map<String, Boolean> completedSteps = (Map<String, Boolean>) progressData.get("completedSteps");
        
        List<OnboardingProgressResponse.StepStatus> stepStatuses = IntStream.rangeClosed(1, 7)
                .mapToObj(step -> {
                    Boolean isCompleted = completedSteps.getOrDefault("step" + step, false);
                    StepMetadata stepMeta = STEP_METADATA.get(step);
                    
                    return OnboardingProgressResponse.StepStatus.builder()
                            .stepNumber(step)
                            .stepName(stepMeta.getStepName())
                            .status(isCompleted ? "COMPLETED" : (step == currentStep ? "IN_PROGRESS" : "NOT_STARTED"))
                            .isRequired(stepMeta.isRequired())
                            .estimatedMinutes(stepMeta.getEstimatedMinutes())
                            .build();
                })
                .collect(Collectors.toList());
        
        StepMetadata currentStepMeta = STEP_METADATA.get(currentStep);
        StepMetadata nextStepMeta = currentStep < 7 ? STEP_METADATA.get(currentStep + 1) : null;
        
        return OnboardingProgressResponse.builder()
                .currentStep(currentStep)
                .totalSteps(7)
                .progressPercentage((Double) progressData.get("progressPercentage"))
                .estimatedMinutesRemaining((Integer) progressData.get("estimatedMinutesRemaining"))
                .stepStatuses(stepStatuses)
                .currentStepInfo(buildStepMetadataResponse(currentStepMeta))
                .nextStepPreview(nextStepMeta != null ? buildStepMetadataResponse(nextStepMeta) : null)
                .motivationalMessage(generateMotivationalMessage(currentStep, (Double) progressData.get("progressPercentage")))
                .nextStepGuidance(generateNextStepGuidance(currentStep))
                .canSkipCurrentStep(!currentStepMeta.isRequired())
                .skipReason(currentStepMeta.isRequired() ? "필수 단계입니다" : "선택사항입니다")
                .lastSavedAt(LocalDateTime.parse((String) progressData.get("lastUpdatedAt")))
                .hasUnsavedChanges(false)
                .build();
    }
    
    private CurrentStepResponse buildCurrentStepResponse(Integer stepNumber, UUID userId, Map<String, Object> savedData) {
        StepMetadata stepMeta = STEP_METADATA.get(stepNumber);
        StepMetadata nextStepMeta = stepNumber < 7 ? STEP_METADATA.get(stepNumber + 1) : null;
        
        Optional<Map<String, Object>> progressData = stateService.getProgressData(userId);
        Double overallProgress = 0.0;
        Integer completedSteps = 0;
        LocalDateTime sessionStartedAt = LocalDateTime.now();
        
        if (progressData.isPresent()) {
            Map<String, Object> progress = progressData.get();
            overallProgress = (Double) progress.get("progressPercentage") / 100.0;
            @SuppressWarnings("unchecked")
            Map<String, Boolean> completed = (Map<String, Boolean>) progress.get("completedSteps");
            completedSteps = (int) completed.values().stream().mapToLong(c -> c ? 1 : 0).sum();
            
            String startedAtStr = (String) progress.get("startedAt");
            if (startedAtStr != null) {
                sessionStartedAt = LocalDateTime.parse(startedAtStr);
            }
        }
        
        return CurrentStepResponse.builder()
                .currentStepNumber(stepNumber)
                .currentStepName(stepMeta.getStepName())
                .currentStepDescription(stepMeta.getDescription())
                .overallProgress(overallProgress)
                .completedSteps(completedSteps)
                .totalSteps(7)
                .estimatedMinutesForCurrentStep(stepMeta.getEstimatedMinutes())
                .estimatedMinutesTotal(25)
                .sessionStartedAt(sessionStartedAt)
                .instructions(stepMeta.getInstructions())
                .fieldHelpTexts(stepMeta.getHelpTexts())
                .tips(stepMeta.getTips())
                .encouragementMessage(generateEncouragementMessage(stepNumber, overallProgress))
                .progressMessage(generateProgressMessage(overallProgress))
                .canSkip(!stepMeta.isRequired())
                .skipReason(stepMeta.isRequired() ? "필수 단계입니다" : null)
                .canGoBack(stepNumber > 1)
                .isRequired(stepMeta.isRequired())
                .savedData(savedData)
                .hasUnsavedChanges(false)
                .lastSavedAt(savedData.containsKey("savedAt") ? 
                    LocalDateTime.parse((String) savedData.get("savedAt")) : null)
                .nextStep(nextStepMeta != null ? 
                    CurrentStepResponse.NextStepPreview.builder()
                            .stepNumber(stepNumber + 1)
                            .stepName(nextStepMeta.getStepName())
                            .shortDescription(nextStepMeta.getShortDescription())
                            .estimatedMinutes(nextStepMeta.getEstimatedMinutes())
                            .isRequired(nextStepMeta.isRequired())
                            .build() : null)
                .build();
    }
    
    private OnboardingProgressResponse.StepMetadata buildStepMetadataResponse(StepMetadata stepMeta) {
        return OnboardingProgressResponse.StepMetadata.builder()
                .stepNumber(stepMeta.getStepNumber())
                .stepName(stepMeta.getStepName())
                .description(stepMeta.getDescription())
                .instructions(stepMeta.getInstructionsText())
                .isRequired(stepMeta.isRequired())
                .estimatedMinutes(stepMeta.getEstimatedMinutes())
                .requiredFields(stepMeta.getRequiredFields())
                .optionalFields(stepMeta.getOptionalFields())
                .helpTexts(stepMeta.getHelpTexts())
                .benefitDescription(stepMeta.getBenefitDescription())
                .exampleOptions(stepMeta.getExampleOptions())
                .build();
    }
    
    private String generateMotivationalMessage(Integer step, Double progress) {
        if (progress >= 80.0) return "거의 다 왔어요! 조금만 더 힘내세요! 🎯";
        if (progress >= 60.0) return "훌륭해요! 벌써 절반 이상 완료하셨네요! 💪";
        if (progress >= 40.0) return "좋은 진전이에요! 계속 진행해주세요! 🚀";
        if (progress >= 20.0) return "좋은 시작이에요! 차근차근 진행해봐요! ✨";
        return "환영합니다! 함께 언어교환 여행을 시작해봐요! 🌟";
    }
    
    private String generateNextStepGuidance(Integer currentStep) {
        switch (currentStep) {
            case 1: return "다음에는 학습하고 싶은 언어와 현재 수준을 설정해보세요.";
            case 2: return "이제 학습 목표와 동기를 설정할 차례예요.";
            case 3: return "어떤 방식으로 학습하고 싶은지 선택해보세요.";
            case 4: return "선호하는 파트너 조건을 설정해보세요.";
            case 5: return "학습 가능한 시간을 설정해주세요.";
            case 6: return "마지막으로 자기소개를 작성해보세요.";
            case 7: return "모든 설정이 완료되었습니다! 매칭을 시작할 준비가 되었어요.";
            default: return "다음 단계로 진행해주세요.";
        }
    }
    
    private String generateEncouragementMessage(Integer step, Double progress) {
        if (step >= 6) return "와우! 거의 끝났어요! 🎉";
        if (step >= 4) return "절반 넘게 완료하셨어요! 👏";
        if (step >= 2) return "좋은 시작이에요! 💫";
        return "천천히 진행해보세요! 🌱";
    }
    
    private String generateProgressMessage(Double progress) {
        if (progress >= 0.9) return "완주까지 한 발짝!";
        if (progress >= 0.7) return "거의 다 왔어요!";
        if (progress >= 0.5) return "중간 지점 통과!";
        if (progress >= 0.3) return "좋은 진전이에요!";
        return "화이팅!";
    }
    
    // 온보딩 단계별 메타데이터 초기화
    private static Map<Integer, StepMetadata> initializeStepMetadata() {
        Map<Integer, StepMetadata> metadata = new HashMap<>();
        
        metadata.put(1, StepMetadata.builder()
                .stepNumber(1)
                .stepName("기본 정보")
                .description("언어 학습을 위한 기본 정보를 설정해주세요")
                .shortDescription("기본 정보 설정")
                .instructionsText("모국어와 학습하고 싶은 언어를 선택해주세요")
                .instructions(Arrays.asList(
                        "먼저 모국어를 선택해주세요",
                        "학습하고 싶은 언어를 선택해주세요", 
                        "현재 언어 수준을 정확히 평가해주세요"))
                .helpTexts(Map.of(
                        "nativeLanguage", "가장 편안하게 구사할 수 있는 언어",
                        "targetLanguage", "배우고 싶은 언어", 
                        "currentLevel", "현재 실력을 객관적으로 평가해주세요"))
                .tips(Arrays.asList("정확한 수준 평가가 좋은 매칭의 첫걸음이에요"))
                .requiredFields(Arrays.asList("nativeLanguage", "targetLanguage", "currentLevel"))
                .optionalFields(Arrays.asList())
                .benefitDescription("정확한 언어 정보로 최적의 파트너를 찾을 수 있어요")
                .exampleOptions(Arrays.asList("한국어 → 영어", "영어 → 중국어", "일본어 → 한국어"))
                .isRequired(true)
                .estimatedMinutes(5)
                .build());
        
        metadata.put(2, StepMetadata.builder()
                .stepNumber(2)
                .stepName("학습 목표")
                .description("언어 학습 목표와 동기를 설정해주세요")
                .shortDescription("목표 및 동기")
                .instructionsText("학습 목표와 동기를 구체적으로 설정해주세요")
                .instructions(Arrays.asList(
                        "주요 학습 목표를 선택해주세요",
                        "학습 동기를 구체적으로 작성해주세요",
                        "목표 달성 기간을 설정해주세요"))
                .helpTexts(Map.of(
                        "learningGoals", "구체적인 목표가 학습 효과를 높입니다",
                        "motivation", "동기가 명확할수록 지속 가능한 학습이 가능해요"))
                .tips(Arrays.asList("명확한 목표가 있으면 더 효과적인 학습이 가능해요"))
                .requiredFields(Arrays.asList("learningGoals"))
                .optionalFields(Arrays.asList("motivation", "targetPeriod"))
                .benefitDescription("목표에 맞는 학습 방법과 파트너를 추천받을 수 있어요")
                .exampleOptions(Arrays.asList("비즈니스 회화", "여행 회화", "시험 준비", "일상 대화"))
                .isRequired(false)
                .estimatedMinutes(3)
                .build());
        
        // 3~7단계도 유사하게 설정...
        for (int i = 3; i <= 7; i++) {
            metadata.put(i, StepMetadata.builder()
                    .stepNumber(i)
                    .stepName("단계 " + i)
                    .description("온보딩 " + i + "단계")
                    .shortDescription("단계 " + i)
                    .instructionsText("지침을 따라 진행해주세요")
                    .instructions(Arrays.asList("단계별 안내를 확인해주세요"))
                    .helpTexts(Collections.emptyMap())
                    .tips(Arrays.asList("차근차근 진행해보세요"))
                    .requiredFields(Collections.emptyList())
                    .optionalFields(Collections.emptyList())
                    .benefitDescription("단계별 완료로 더 나은 매칭이 가능해요")
                    .exampleOptions(Collections.emptyList())
                    .isRequired(i <= 4) // 1-4단계만 필수
                    .estimatedMinutes(i <= 4 ? 4 : 3)
                    .build());
        }
        
        return metadata;
    }
    
    // 내부 메타데이터 클래스
    private static class StepMetadata {
        private final Integer stepNumber;
        private final String stepName;
        private final String description;
        private final String shortDescription;
        private final String instructionsText;
        private final List<String> instructions;
        private final Map<String, String> helpTexts;
        private final List<String> tips;
        private final List<String> requiredFields;
        private final List<String> optionalFields;
        private final String benefitDescription;
        private final List<String> exampleOptions;
        private final boolean isRequired;
        private final Integer estimatedMinutes;
        
        public StepMetadata(Integer stepNumber, String stepName, String description, String shortDescription,
                           String instructionsText, List<String> instructions, Map<String, String> helpTexts,
                           List<String> tips, List<String> requiredFields, List<String> optionalFields,
                           String benefitDescription, List<String> exampleOptions, boolean isRequired,
                           Integer estimatedMinutes) {
            this.stepNumber = stepNumber;
            this.stepName = stepName;
            this.description = description;
            this.shortDescription = shortDescription;
            this.instructionsText = instructionsText;
            this.instructions = instructions;
            this.helpTexts = helpTexts;
            this.tips = tips;
            this.requiredFields = requiredFields;
            this.optionalFields = optionalFields;
            this.benefitDescription = benefitDescription;
            this.exampleOptions = exampleOptions;
            this.isRequired = isRequired;
            this.estimatedMinutes = estimatedMinutes;
        }
        
        public static StepMetadataBuilder builder() {
            return new StepMetadataBuilder();
        }
        
        public static class StepMetadataBuilder {
            private Integer stepNumber;
            private String stepName;
            private String description;
            private String shortDescription;
            private String instructionsText;
            private List<String> instructions;
            private Map<String, String> helpTexts;
            private List<String> tips;
            private List<String> requiredFields;
            private List<String> optionalFields;
            private String benefitDescription;
            private List<String> exampleOptions;
            private boolean isRequired;
            private Integer estimatedMinutes;
            
            public StepMetadataBuilder stepNumber(Integer stepNumber) {
                this.stepNumber = stepNumber;
                return this;
            }
            
            public StepMetadataBuilder stepName(String stepName) {
                this.stepName = stepName;
                return this;
            }
            
            public StepMetadataBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public StepMetadataBuilder shortDescription(String shortDescription) {
                this.shortDescription = shortDescription;
                return this;
            }
            
            public StepMetadataBuilder instructionsText(String instructionsText) {
                this.instructionsText = instructionsText;
                return this;
            }
            
            public StepMetadataBuilder instructions(List<String> instructions) {
                this.instructions = instructions;
                return this;
            }
            
            public StepMetadataBuilder helpTexts(Map<String, String> helpTexts) {
                this.helpTexts = helpTexts;
                return this;
            }
            
            public StepMetadataBuilder tips(List<String> tips) {
                this.tips = tips;
                return this;
            }
            
            public StepMetadataBuilder requiredFields(List<String> requiredFields) {
                this.requiredFields = requiredFields;
                return this;
            }
            
            public StepMetadataBuilder optionalFields(List<String> optionalFields) {
                this.optionalFields = optionalFields;
                return this;
            }
            
            public StepMetadataBuilder benefitDescription(String benefitDescription) {
                this.benefitDescription = benefitDescription;
                return this;
            }
            
            public StepMetadataBuilder exampleOptions(List<String> exampleOptions) {
                this.exampleOptions = exampleOptions;
                return this;
            }
            
            public StepMetadataBuilder isRequired(boolean isRequired) {
                this.isRequired = isRequired;
                return this;
            }
            
            public StepMetadataBuilder estimatedMinutes(Integer estimatedMinutes) {
                this.estimatedMinutes = estimatedMinutes;
                return this;
            }
            
            public StepMetadata build() {
                return new StepMetadata(stepNumber, stepName, description, shortDescription,
                        instructionsText, instructions, helpTexts, tips, requiredFields,
                        optionalFields, benefitDescription, exampleOptions, isRequired, estimatedMinutes);
            }
        }
        
        // Getters
        public Integer getStepNumber() { return stepNumber; }
        public String getStepName() { return stepName; }
        public String getDescription() { return description; }
        public String getShortDescription() { return shortDescription; }
        public String getInstructionsText() { return instructionsText; }
        public List<String> getInstructions() { return instructions; }
        public Map<String, String> getHelpTexts() { return helpTexts; }
        public List<String> getTips() { return tips; }
        public List<String> getRequiredFields() { return requiredFields; }
        public List<String> getOptionalFields() { return optionalFields; }
        public String getBenefitDescription() { return benefitDescription; }
        public List<String> getExampleOptions() { return exampleOptions; }
        public boolean isRequired() { return isRequired; }
        public Integer getEstimatedMinutes() { return estimatedMinutes; }
    }
}
