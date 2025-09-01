package com.studymate.domain.leveltest.domain.repository;

import com.studymate.domain.leveltest.entity.LevelTest;
import com.studymate.domain.leveltest.entity.LevelTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelTestResultRepository extends JpaRepository<LevelTestResult, Long> {

    List<LevelTestResult> findByLevelTestOrderByQuestionNumber(LevelTest levelTest);

    Optional<LevelTestResult> findByLevelTestAndQuestionNumber(LevelTest levelTest, Integer questionNumber);

    @Query("SELECT ltr FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId ORDER BY ltr.questionNumber")
    List<LevelTestResult> findByTestIdOrderByQuestionNumber(@Param("testId") Long testId);

    @Query("SELECT COUNT(ltr) FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId AND ltr.isCorrect = true")
    Long countCorrectAnswersByTestId(@Param("testId") Long testId);

    @Query("SELECT COUNT(ltr) FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId")
    Long countTotalAnswersByTestId(@Param("testId") Long testId);

    @Query("SELECT ltr FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId AND ltr.skillCategory = :skillCategory")
    List<LevelTestResult> findByTestIdAndSkillCategory(@Param("testId") Long testId, 
                                                      @Param("skillCategory") String skillCategory);

    @Query("SELECT ltr FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId AND ltr.difficultyLevel = :difficultyLevel")
    List<LevelTestResult> findByTestIdAndDifficultyLevel(@Param("testId") Long testId, 
                                                        @Param("difficultyLevel") String difficultyLevel);

    @Query("SELECT " +
           "CASE WHEN COUNT(ltr) > 0 THEN " +
           "CAST(SUM(CASE WHEN ltr.isCorrect = true THEN 1 ELSE 0 END) AS double) / COUNT(ltr) * 100 " +
           "ELSE NULL END " +
           "FROM LevelTestResult ltr " +
           "WHERE ltr.levelTest.testId = :testId AND ltr.skillCategory = :skillCategory")
    Optional<Double> getAccuracyByTestIdAndSkillCategory(@Param("testId") Long testId, @Param("skillCategory") String skillCategory);

    @Query("SELECT ltr.skillCategory, " +
           "CASE WHEN COUNT(ltr) > 0 THEN " +
           "CAST(SUM(CASE WHEN ltr.isCorrect = true THEN 1 ELSE 0 END) AS double) / COUNT(ltr) * 100 " +
           "ELSE 0.0 END " +
           "FROM LevelTestResult ltr " +
           "WHERE ltr.levelTest.testId = :testId " +
           "GROUP BY ltr.skillCategory")
    List<Object[]> getAccuracyBySkillCategory(@Param("testId") Long testId);

    @Query("SELECT SUM(ltr.pointsEarned) FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId")
    Optional<Integer> getTotalPointsEarnedByTestId(@Param("testId") Long testId);

    @Query("SELECT SUM(ltr.maxPoints) FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId")
    Optional<Integer> getTotalMaxPointsByTestId(@Param("testId") Long testId);

    @Query("SELECT AVG(ltr.responseTimeSeconds) FROM LevelTestResult ltr WHERE ltr.levelTest.testId = :testId AND ltr.responseTimeSeconds IS NOT NULL")
    Optional<Double> getAverageResponseTimeByTestId(@Param("testId") Long testId);
}