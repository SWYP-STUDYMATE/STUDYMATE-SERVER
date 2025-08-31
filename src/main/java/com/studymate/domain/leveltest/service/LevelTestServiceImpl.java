package com.studymate.domain.leveltest.service;

import com.studymate.domain.leveltest.domain.dto.request.StartLevelTestRequest;
import com.studymate.domain.leveltest.domain.dto.request.SubmitAnswerRequest;
import com.studymate.domain.leveltest.domain.dto.response.*;
import com.studymate.domain.leveltest.domain.repository.LevelTestRepository;
import com.studymate.domain.leveltest.domain.repository.LevelTestResultRepository;
import com.studymate.domain.leveltest.entity.LevelTest;
import com.studymate.domain.leveltest.entity.LevelTestResult;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelTestServiceImpl implements LevelTestService {

    private final UserRepository userRepository;
    private final LevelTestRepository levelTestRepository;
    private final LevelTestResultRepository levelTestResultRepository;

    @Override
    public String generateVoiceTestPrompt(String level, String language) {
        // TODO: AI를 활용한 음성 테스트 프롬프트 생성
        return String.format("Please read the following text aloud in %s at %s level:", language, level);
    }

    @Override
    public LevelTestResponse startLevelTest(UUID userId, StartLevelTestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        LevelTest levelTest = LevelTest.builder()
                .user(user)
                .testType(request.getTestType())
                .languageCode(request.getLanguageCode())
                .testLevel(request.getTestLevel())
                .totalQuestions(request.getTotalQuestions())
                .startedAt(LocalDateTime.now())
                .build();

        LevelTest savedLevelTest = levelTestRepository.save(levelTest);
        
        // 테스트 문제 생성 및 저장
        generateTestQuestions(savedLevelTest, request);

        return convertToLevelTestResponse(savedLevelTest);
    }

    @Override
    public LevelTestResponse submitAnswer(UUID userId, SubmitAnswerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        LevelTest levelTest = levelTestRepository.findByUserAndTestIdAndIsCompleted(user, request.getTestId(), false)
                .orElseThrow(() -> new NotFoundException("NOT FOUND ACTIVE TEST"));

        LevelTestResult testResult = levelTestResultRepository.findByLevelTestAndQuestionNumber(levelTest, request.getQuestionNumber())
                .orElseThrow(() -> new NotFoundException("NOT FOUND TEST QUESTION"));

        // 답안 제출
        testResult.submitAnswer(request.getUserAnswer(), request.getUserAudioUrl(), request.getResponseTimeSeconds());
        levelTestResultRepository.save(testResult);

        // 진행 상황 업데이트
        updateTestProgress(levelTest);

        return convertToLevelTestResponse(levelTest);
    }

    @Override
    public LevelTestResponse completeLevelTest(UUID userId, Long testId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        LevelTest levelTest = levelTestRepository.findByUserAndTestIdAndIsCompleted(user, testId, false)
                .orElseThrow(() -> new NotFoundException("NOT FOUND ACTIVE TEST"));

        // 테스트 결과 계산
        calculateTestResults(levelTest);

        return convertToLevelTestResponse(levelTest);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelTestResponse getLevelTest(UUID userId, Long testId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        LevelTest levelTest = levelTestRepository.findById(testId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND TEST"));

        if (!levelTest.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("ACCESS DENIED");
        }

        return convertToLevelTestResponse(levelTest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LevelTestResponse> getUserLevelTests(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        Page<LevelTest> levelTests = levelTestRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return levelTests.map(this::convertToLevelTestResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelTestSummaryResponse getUserLevelTestSummary(UUID userId) {
        List<String> testedLanguages = levelTestRepository.findTestedLanguagesByUserId(userId);
        Long totalCompletedTests = levelTestRepository.countCompletedTestsByUserId(userId);
        
        Map<String, String> latestLevels = new HashMap<>();
        Map<String, Double> averageAccuracies = new HashMap<>();
        
        for (String language : testedLanguages) {
            // 최신 레벨
            Optional<LevelTest> latestTest = levelTestRepository.findLatestCompletedTest(userId, language, "COMPREHENSIVE");
            if (latestTest.isPresent()) {
                latestLevels.put(language, latestTest.get().getEstimatedLevel());
            }
            
            // 평균 정확도
            Optional<Double> avgAccuracy = levelTestRepository.getAverageAccuracyByUserAndLanguage(userId, language);
            if (avgAccuracy.isPresent()) {
                averageAccuracies.put(language, avgAccuracy.get());
            }
        }
        
        // 최근 테스트 목록
        List<LevelTest> recentTests = levelTestRepository.findCompletedTestsByUserId(userId)
                .stream()
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(5)
                .collect(Collectors.toList());
        
        List<LevelTestSummaryResponse.RecentTestSummary> recentTestSummaries = recentTests.stream()
                .map(test -> new LevelTestSummaryResponse.RecentTestSummary(
                        test.getTestId(),
                        test.getTestType().name(),
                        test.getLanguageCode(),
                        test.getEstimatedLevel(),
                        test.getAccuracyPercentage(),
                        test.getCompletedAt()
                ))
                .collect(Collectors.toList());
        
        return new LevelTestSummaryResponse(testedLanguages, totalCompletedTests, 
                                          latestLevels, averageAccuracies, recentTestSummaries);
    }

    private void generateTestQuestions(LevelTest levelTest, StartLevelTestRequest request) {
        // 실제 구현에서는 AI나 문제 은행에서 문제를 생성해야 함
        // 현재는 샘플 문제 생성
        for (int i = 1; i <= request.getTotalQuestions(); i++) {
            LevelTestResult testResult = LevelTestResult.builder()
                    .levelTest(levelTest)
                    .questionNumber(i)
                    .questionType("MULTIPLE_CHOICE")
                    .questionText("Sample question " + i + " for " + request.getLanguageCode())
                    .correctAnswer("A")
                    .difficultyLevel(determineDifficultyLevel(i, request.getTotalQuestions()))
                    .skillCategory(determineSkillCategory(i))
                    .maxPoints(10)
                    .explanation("Sample explanation for question " + i)
                    .build();
            
            levelTestResultRepository.save(testResult);
        }
    }

    private String determineDifficultyLevel(int questionNumber, int totalQuestions) {
        if (questionNumber <= totalQuestions * 0.3) {
            return "EASY";
        } else if (questionNumber <= totalQuestions * 0.7) {
            return "MEDIUM";
        } else {
            return "HARD";
        }
    }

    private String determineSkillCategory(int questionNumber) {
        String[] categories = {"GRAMMAR", "VOCABULARY", "COMPREHENSION", "PRONUNCIATION"};
        return categories[questionNumber % categories.length];
    }

    private void updateTestProgress(LevelTest levelTest) {
        Long correctAnswers = levelTestResultRepository.countCorrectAnswersByTestId(levelTest.getTestId());
        Long totalAnswers = levelTestResultRepository.countTotalAnswersByTestId(levelTest.getTestId());
        
        if (totalAnswers > 0) {
            double accuracyPercentage = (correctAnswers.doubleValue() / totalAnswers.doubleValue()) * 100;
            levelTest.updateProgress(correctAnswers.intValue(), accuracyPercentage);
            levelTestRepository.save(levelTest);
        }
    }

    private void calculateTestResults(LevelTest levelTest) {
        Long correctAnswers = levelTestResultRepository.countCorrectAnswersByTestId(levelTest.getTestId());
        Long totalAnswers = levelTestResultRepository.countTotalAnswersByTestId(levelTest.getTestId());
        
        if (totalAnswers > 0) {
            double accuracyPercentage = (correctAnswers.doubleValue() / totalAnswers.doubleValue()) * 100;
            String estimatedLevel = determineLevel(accuracyPercentage);
            int estimatedScore = (int) Math.round(accuracyPercentage);
            
            // 피드백 생성
            String feedback = generateFeedback(accuracyPercentage, levelTest);
            String strengths = generateStrengths(levelTest);
            String weaknesses = generateWeaknesses(levelTest);
            String recommendations = generateRecommendations(accuracyPercentage, levelTest);
            
            // 테스트 시간 계산
            int testDurationSeconds = calculateTestDuration(levelTest);
            
            levelTest.completeTest(correctAnswers.intValue(), accuracyPercentage, 
                                 estimatedLevel, estimatedScore, testDurationSeconds,
                                 feedback, strengths, weaknesses, recommendations);
            
            levelTestRepository.save(levelTest);
        }
    }

    private String determineLevel(double accuracyPercentage) {
        if (accuracyPercentage >= 90) return "C2";
        if (accuracyPercentage >= 80) return "C1";
        if (accuracyPercentage >= 70) return "B2";
        if (accuracyPercentage >= 60) return "B1";
        if (accuracyPercentage >= 50) return "A2";
        return "A1";
    }

    private String generateFeedback(double accuracyPercentage, LevelTest levelTest) {
        if (accuracyPercentage >= 80) {
            return "훌륭한 성과를 보여주셨습니다! 해당 언어에 대한 이해도가 매우 높습니다.";
        } else if (accuracyPercentage >= 60) {
            return "좋은 성과입니다. 몇 가지 영역에서 더 학습하면 더욱 향상될 수 있습니다.";
        } else {
            return "기초를 더 탄탄히 하고 꾸준한 학습이 필요합니다.";
        }
    }

    private String generateStrengths(LevelTest levelTest) {
        List<Object[]> accuracyBySkill = levelTestResultRepository.getAccuracyBySkillCategory(levelTest.getTestId());
        
        String bestSkill = accuracyBySkill.stream()
                .max(Comparator.comparing(arr -> (Double) arr[1]))
                .map(arr -> (String) arr[0])
                .orElse("VOCABULARY");
                
        return bestSkill + " 영역에서 강점을 보이고 있습니다.";
    }

    private String generateWeaknesses(LevelTest levelTest) {
        List<Object[]> accuracyBySkill = levelTestResultRepository.getAccuracyBySkillCategory(levelTest.getTestId());
        
        String weakestSkill = accuracyBySkill.stream()
                .min(Comparator.comparing(arr -> (Double) arr[1]))
                .map(arr -> (String) arr[0])
                .orElse("GRAMMAR");
                
        return weakestSkill + " 영역에서 더 많은 학습이 필요합니다.";
    }

    private String generateRecommendations(double accuracyPercentage, LevelTest levelTest) {
        if (accuracyPercentage >= 80) {
            return "상위 레벨의 콘텐츠에 도전해보세요. 원어민과의 대화 연습을 늘려보는 것을 추천합니다.";
        } else if (accuracyPercentage >= 60) {
            return "기본기를 더 탄탄히 하고 다양한 주제의 콘텐츠를 접해보세요.";
        } else {
            return "기초 문법과 기본 어휘를 중심으로 체계적인 학습이 필요합니다.";
        }
    }

    private int calculateTestDuration(LevelTest levelTest) {
        if (levelTest.getStartedAt() != null && levelTest.getCompletedAt() != null) {
            return (int) java.time.Duration.between(levelTest.getStartedAt(), LocalDateTime.now()).getSeconds();
        }
        return 0;
    }

    private LevelTestResponse convertToLevelTestResponse(LevelTest levelTest) {
        List<LevelTestResultResponse> results = levelTest.getTestResults() != null ? 
                levelTest.getTestResults().stream()
                        .map(this::convertToLevelTestResultResponse)
                        .collect(Collectors.toList()) : new ArrayList<>();

        return new LevelTestResponse(
                levelTest.getTestId(),
                levelTest.getTestType().name(),
                levelTest.getLanguageCode(),
                levelTest.getTestLevel(),
                levelTest.getTotalQuestions(),
                levelTest.getCorrectAnswers(),
                levelTest.getAccuracyPercentage(),
                levelTest.getEstimatedLevel(),
                levelTest.getEstimatedScore(),
                levelTest.getTestDurationSeconds(),
                levelTest.getStartedAt(),
                levelTest.getCompletedAt(),
                levelTest.getIsCompleted(),
                levelTest.getFeedback(),
                levelTest.getStrengths(),
                levelTest.getWeaknesses(),
                levelTest.getRecommendations(),
                results
        );
    }

    private LevelTestResultResponse convertToLevelTestResultResponse(LevelTestResult result) {
        return new LevelTestResultResponse(
                result.getResultId(),
                result.getQuestionNumber(),
                result.getQuestionType(),
                result.getQuestionText(),
                result.getQuestionAudioUrl(),
                result.getQuestionImageUrl(),
                result.getCorrectAnswer(),
                result.getUserAnswer(),
                result.getUserAudioUrl(),
                result.getIsCorrect(),
                result.getPointsEarned(),
                result.getMaxPoints(),
                result.getResponseTimeSeconds(),
                result.getDifficultyLevel(),
                result.getSkillCategory(),
                result.getExplanation()
        );
    }
    
    @Override
    @Transactional
    public LevelTestResponse startVoiceLevelTest(UUID userId, String languageCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));
        
        LevelTest levelTest = LevelTest.builder()
                .user(user)
                .testType(LevelTest.TestType.VOICE)
                .languageCode(languageCode)
                .testLevel("ADAPTIVE")
                .totalQuestions(1)
                .startedAt(LocalDateTime.now())
                .build();
        
        levelTest.setAsVoiceTest();
        LevelTest savedLevelTest = levelTestRepository.save(levelTest);
        
        return convertToLevelTestResponse(savedLevelTest);
    }

    @Override
    @Transactional
    public LevelTestResponse uploadVoiceRecording(UUID userId, Long testId, org.springframework.web.multipart.MultipartFile audioFile) {
        LevelTest levelTest = levelTestRepository.findByTestIdAndUserId(testId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Level test not found"));
        
        if (levelTest.getTestType() != LevelTest.TestType.VOICE) {
            throw new IllegalArgumentException("This is not a voice test");
        }
        
        // TODO: 실제로는 파일 저장 및 처리 로직 구현 필요
        // 현재는 임시 URL로 처리
        String audioFileUrl = "/audio/" + testId + "_" + System.currentTimeMillis() + ".wav";
        levelTest.updateAudioFile(audioFileUrl);
        
        levelTestRepository.save(levelTest);
        
        return convertToLevelTestResponse(levelTest);
    }

    @Override
    @Transactional
    public LevelTestResponse processVoiceTest(UUID userId, Long testId) {
        LevelTest levelTest = levelTestRepository.findByTestIdAndUserId(testId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Level test not found"));
        
        if (levelTest.getTestType() != LevelTest.TestType.VOICE) {
            throw new IllegalArgumentException("This is not a voice test");
        }
        
        // 음성 처리 로직 (AI 분석 등)
        // 임시로 기본 구현
        levelTest.setStatus(LevelTest.TestStatus.COMPLETED);
        levelTest.setCompletedAt(LocalDateTime.now());
        
        // 기본 결과 생성
        levelTest.setAccuracyPercentage(75.0);
        levelTest.setTotalScore(75);
        levelTest.setMaxScore(100);
        
        levelTestRepository.save(levelTest);
        
        return convertToLevelTestResponse(levelTest);
    }
}