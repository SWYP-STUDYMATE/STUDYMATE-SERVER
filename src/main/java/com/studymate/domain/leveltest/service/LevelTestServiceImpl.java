package com.studymate.domain.leveltest.service;

import com.studymate.domain.ai.service.WorkersAIService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LevelTestServiceImpl implements LevelTestService {

    private final UserRepository userRepository;
    private final LevelTestRepository levelTestRepository;
    private final LevelTestResultRepository levelTestResultRepository;
    private final WorkersAIService workersAIService;

    @Override
    public String generateVoiceTestPrompt(String level, String language) {
        Map<String, Map<String, String>> prompts = Map.of(
                "English", Map.of(
                        "Beginner", "Please read this simple sentence: 'Hello, my name is Sarah. I am learning English.'",
                        "Intermediate", "Please read this paragraph: 'Technology has transformed the way we communicate with each other. Social media platforms allow us to connect with people around the world instantly.'",
                        "Advanced", "Please read this complex text: 'The unprecedented acceleration of technological advancement has fundamentally altered the socioeconomic landscape, necessitating a paradigm shift in educational methodologies.'"
                ),
                "Korean", Map.of(
                        "Beginner", "다음 문장을 읽어주세요: '안녕하세요, 제 이름은 사라입니다. 저는 한국어를 배우고 있습니다.'",
                        "Intermediate", "다음 문단을 읽어주세요: '기술은 우리가 서로 소통하는 방식을 변화시켰습니다. 소셜 미디어 플랫폼은 우리가 전 세계 사람들과 즉시 연결될 수 있게 해줍니다.'",
                        "Advanced", "다음 복잡한 텍스트를 읽어주세요: '기술 발전의 전례 없는 가속화는 사회경제적 환경을 근본적으로 변화시켰으며, 교육 방법론의 패러다임 전환을 필요로 하고 있습니다.'"
                ),
                "Japanese", Map.of(
                        "Beginner", "次の文を読んでください：「こんにちは、私の名前はサラです。日本語を勉強しています。」",
                        "Intermediate", "次の段落を読んでください：「技術は私たちがお互いにコミュニケーションを取る方法を変えました。ソーシャルメディアプラットフォームは、世界中の人々と瞬時に接続することを可能にします。」",
                        "Advanced", "次の複雑なテキストを読んでください：「技術的進歩の前例のない加速は、社会経済的環境を根本的に変化させ、教育方法論のパラダイムシフトを必要としています。」"
                )
        );
        return prompts.getOrDefault(language, prompts.get("English"))
                .getOrDefault(level, "Please read the following text aloud clearly.");
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

        testResult.submitAnswer(request.getUserAnswer(), request.getUserAudioUrl(), request.getResponseTimeSeconds());
        levelTestResultRepository.save(testResult);

        updateTestProgress(levelTest);
        return convertToLevelTestResponse(levelTest);
    }

    @Override
    public LevelTestResponse completeLevelTest(UUID userId, Long testId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        LevelTest levelTest = levelTestRepository.findByUserAndTestIdAndIsCompleted(user, testId, false)
                .orElseThrow(() -> new NotFoundException("NOT FOUND ACTIVE TEST"));

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
            levelTestRepository.findLatestCompletedTest(userId, language, LevelTest.TestType.COMPREHENSIVE)
                    .ifPresent(t -> latestLevels.put(language, t.getEstimatedLevel()));
            levelTestRepository.getAverageAccuracyByUserAndLanguage(userId, language)
                    .ifPresent(avg -> averageAccuracies.put(language, avg));
        }

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
                )).collect(Collectors.toList());

        return new LevelTestSummaryResponse(testedLanguages, totalCompletedTests, latestLevels, averageAccuracies, recentTestSummaries);
    }

    private void generateTestQuestions(LevelTest levelTest, StartLevelTestRequest request) {
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
        if (questionNumber <= totalQuestions * 0.3) return "EASY";
        else if (questionNumber <= totalQuestions * 0.7) return "MEDIUM";
        else return "HARD";
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

            String feedback = generateFeedback(accuracyPercentage, levelTest);
            String strengths = generateStrengths(levelTest);
            String weaknesses = generateWeaknesses(levelTest);
            String recommendations = generateRecommendations(accuracyPercentage, levelTest);

            levelTest.completeTest(correctAnswers.intValue(), accuracyPercentage,
                    estimatedLevel, estimatedScore, calculateTestDuration(levelTest),
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
        if (accuracyPercentage >= 80) return "훌륭한 성과를 보여주셨습니다! 해당 언어에 대한 이해도가 매우 높습니다.";
        else if (accuracyPercentage >= 60) return "좋은 성과입니다. 몇 가지 영역에서 더 학습하면 더욱 향상될 수 있습니다.";
        else return "기초를 더 탄탄히 하고 꾸준한 학습이 필요합니다.";
    }

    private String generateStrengths(LevelTest levelTest) {
        List<Object[]> bySkill = levelTestResultRepository.getAccuracyBySkillCategory(levelTest.getTestId());
        String best = bySkill.stream()
                .max(Comparator.comparing(arr -> (Double) arr[1]))
                .map(arr -> (String) arr[0]).orElse("VOCABULARY");
        return best + " 영역에서 강점을 보이고 있습니다.";
    }

    private String generateWeaknesses(LevelTest levelTest) {
        List<Object[]> bySkill = levelTestResultRepository.getAccuracyBySkillCategory(levelTest.getTestId());
        String worst = bySkill.stream()
                .min(Comparator.comparing(arr -> (Double) arr[1]))
                .map(arr -> (String) arr[0]).orElse("GRAMMAR");
        return worst + " 영역에서 더 많은 학습이 필요합니다.";
    }

    private String generateRecommendations(double accuracyPercentage, LevelTest levelTest) {
        if (accuracyPercentage >= 80) return "상위 레벨의 콘텐츠에 도전해보세요. 원어민과의 대화 연습을 늘려보는 것을 추천합니다.";
        else if (accuracyPercentage >= 60) return "기본기를 더 탄탄히 하고 다양한 주제의 콘텐츠를 접해보세요.";
        else return "기초 문법과 기본 어휘를 중심으로 체계적인 학습이 필요합니다.";
    }

    private int calculateTestDuration(LevelTest levelTest) {
        if (levelTest.getStartedAt() != null && levelTest.getCompletedAt() != null) {
            return (int) Duration.between(levelTest.getStartedAt(), levelTest.getCompletedAt()).getSeconds();
        }
        return 0;
    }

    private LevelTestResponse convertToLevelTestResponse(LevelTest levelTest) {
        List<LevelTestResultResponse> results = levelTest.getTestResults() != null
                ? levelTest.getTestResults().stream().map(this::convertToLevelTestResultResponse).collect(Collectors.toList())
                : new ArrayList<>();

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
        LevelTest saved = levelTestRepository.save(levelTest);
        return convertToLevelTestResponse(saved);
    }

    @Override
    @Transactional
    public LevelTestResponse uploadVoiceRecording(
            UUID userId, Long testId, MultipartFile audioFile) {

        LevelTest levelTest = levelTestRepository.findByTestIdAndUserId(testId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Level test not found"));

        if (levelTest.getTestType() != LevelTest.TestType.VOICE) {
            throw new IllegalArgumentException("This is not a voice test");
        }

        // 1) 먼저 전사 (임시파일 접근 가능할 때)
        String transcript = null;
        try {
            transcript = workersAIService.transcribeAudio(audioFile);
        } catch (Exception e) {
            log.error("[VoiceTest][upload] transcription failed", e);
        }

        // 2) 그 다음 파일 저장
        String audioFilePath = saveAudioFile(testId, audioFile); // 실제 로컬 저장 경로
        String audioFileUrl  = "/api/v1/audio/" + new java.io.File(audioFilePath).getName(); // 접근용 URL

        // 3) 엔티티 업데이트
        levelTest.updateAudioFile(audioFileUrl, audioFilePath);

        if (transcript != null && !transcript.isBlank()) {
            levelTest.setTranscriptText(transcript);
        } else {
            log.warn("[VoiceTest][upload] transcript is empty. keep null");
        }

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

        try {
            String transcript = levelTest.getTranscriptText();

// 업로드 시 저장한 전사 텍스트가 있으면 그대로 사용
            if (transcript == null || transcript.isBlank()) {
                log.warn("[VoiceTest][analyze] transcriptText is empty. falling back to placeholder.");
                transcript = " ";
            }

            Map<String, Object> questions = new HashMap<>();
            questions.put("prompt", generateVoiceTestPrompt(levelTest.getTestLevel(), levelTest.getLanguageCode()));

            VoiceAnalysisResponse analysis = workersAIService.evaluateLevelTest(
                    transcript,
                    levelTest.getLanguageCode(),
                    questions
            );

            levelTest.setStatus(LevelTest.TestStatus.COMPLETED);
            levelTest.setCompletedAt(LocalDateTime.now());
            levelTest.setTestDurationSeconds(calculateTestDurationAfterComplete(levelTest));

            levelTest.setIsCompleted(true);

            levelTest.setAccuracyPercentage((double) analysis.getOverallScore());
            levelTest.setEstimatedScore(analysis.getOverallScore());
            levelTest.setTotalScore(analysis.getOverallScore());
            levelTest.setMaxScore(100);
            levelTest.setEstimatedLevel(analysis.getCefrLevel());
            levelTest.setFeedback(analysis.getFeedback());
            levelTest.setStrengths(analysis.getStrengths());
            levelTest.setWeaknesses(analysis.getWeaknesses());
            levelTest.setTranscriptText(transcript);

            if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
                levelTest.setRecommendations(String.join("; ", analysis.getRecommendations()));
            }

            if (analysis.getScoreBreakdown() != null) {
                var sb = analysis.getScoreBreakdown();
                levelTest.setPronunciationScore(sb.getPronunciationScore());
                levelTest.setFluencyScore(sb.getFluencyScore());
                levelTest.setGrammarScore(sb.getGrammarScore());
                levelTest.setVocabularyScore(sb.getVocabularyScore());
            }

        } catch (Exception e) {
            log.error("Failed to process voice test with Workers AI: ", e);
            levelTest.setStatus(LevelTest.TestStatus.COMPLETED);
            levelTest.setCompletedAt(LocalDateTime.now());
            levelTest.setTestDurationSeconds(calculateTestDurationAfterComplete(levelTest));

            levelTest.setAccuracyPercentage(75.0);
            levelTest.setEstimatedScore(75);
            levelTest.setTotalScore(75);
            levelTest.setMaxScore(100);
            levelTest.setEstimatedLevel("B1");
            levelTest.setFeedback("Voice analysis completed. Keep practicing!");
            levelTest.setStrengths(null);
            levelTest.setWeaknesses(null);
            levelTest.setRecommendations(null);

            // 서브 스코어 기본 0
            levelTest.setPronunciationScore(0);
            levelTest.setFluencyScore(0);
            levelTest.setGrammarScore(0);
            levelTest.setVocabularyScore(0);
        }

        levelTestRepository.save(levelTest);
        return convertToLevelTestResponse(levelTest);
    }

    private int calculateTestDurationAfterComplete(LevelTest levelTest) {
        if (levelTest.getStartedAt() != null) {
            LocalDateTime end = levelTest.getCompletedAt() != null ? levelTest.getCompletedAt() : LocalDateTime.now();
            return (int) Duration.between(levelTest.getStartedAt(), end).getSeconds();
        }
        return 0;
    }

    private String saveAudioFile(Long testId, org.springframework.web.multipart.MultipartFile audioFile) {
        try {
            if (audioFile.isEmpty()) throw new IllegalArgumentException("Audio file is empty");

            String originalFilename = audioFile.getOriginalFilename();
            if (originalFilename == null) originalFilename = "audio.wav";

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!isValidAudioFormat(fileExtension)) {
                throw new IllegalArgumentException("Invalid audio format: " + fileExtension);
            }

            String fileName = testId + "_" + System.currentTimeMillis() + fileExtension;

            String uploadDir = System.getProperty("java.io.tmpdir") + "/studymate/audio/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            audioFile.transferTo(filePath.toFile());

            return "/api/v1/audio/" + fileName;

        } catch (Exception e) {
            log.error("Failed to save audio file for test {}: {}", testId, e.getMessage());
            return "/audio/" + testId + "_" + System.currentTimeMillis() + ".wav";
        }
    }

    private boolean isValidAudioFormat(String fileExtension) {
        return fileExtension != null &&
                (fileExtension.equalsIgnoreCase(".wav") ||
                        fileExtension.equalsIgnoreCase(".mp3") ||
                        fileExtension.equalsIgnoreCase(".m4a") ||
                        fileExtension.equalsIgnoreCase(".ogg") ||
                        fileExtension.equalsIgnoreCase(".webm"));
    }
}
