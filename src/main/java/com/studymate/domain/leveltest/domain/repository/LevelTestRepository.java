package com.studymate.domain.leveltest.domain.repository;

import com.studymate.domain.leveltest.entity.LevelTest;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LevelTestRepository extends JpaRepository<LevelTest, Long> {

    Page<LevelTest> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<LevelTest> findByUserAndLanguageCodeOrderByCreatedAtDesc(User user, String languageCode);

    Optional<LevelTest> findByUserAndTestIdAndIsCompleted(User user, Long testId, Boolean isCompleted);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.isCompleted = true")
    List<LevelTest> findCompletedTestsByUserId(@Param("userId") UUID userId);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.languageCode = :languageCode AND lt.isCompleted = true ORDER BY lt.completedAt DESC")
    List<LevelTest> findCompletedTestsByUserIdAndLanguage(@Param("userId") UUID userId, 
                                                         @Param("languageCode") String languageCode);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.testType = :testType AND lt.isCompleted = true ORDER BY lt.completedAt DESC")
    List<LevelTest> findCompletedTestsByUserIdAndType(@Param("userId") UUID userId, 
                                                     @Param("testType") String testType);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.isCompleted = false ORDER BY lt.createdAt DESC")
    List<LevelTest> findIncompleteTestsByUserId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT lt.languageCode FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.isCompleted = true")
    List<String> findTestedLanguagesByUserId(@Param("userId") UUID userId);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.languageCode = :languageCode AND lt.testType = :testType AND lt.isCompleted = true ORDER BY lt.completedAt DESC LIMIT 1")
    Optional<LevelTest> findLatestCompletedTest(@Param("userId") UUID userId, 
                                              @Param("languageCode") String languageCode, 
                                              @Param("testType") String testType);

    @Query("SELECT AVG(lt.accuracyPercentage) FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.languageCode = :languageCode AND lt.isCompleted = true")
    Optional<Double> getAverageAccuracyByUserAndLanguage(@Param("userId") UUID userId, 
                                                        @Param("languageCode") String languageCode);

    @Query("SELECT COUNT(lt) FROM LevelTest lt WHERE lt.user.userId = :userId AND lt.isCompleted = true")
    Long countCompletedTestsByUserId(@Param("userId") UUID userId);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.completedAt BETWEEN :startDate AND :endDate ORDER BY lt.completedAt DESC")
    List<LevelTest> findTestsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lt FROM LevelTest lt WHERE lt.testId = :testId AND lt.user.userId = :userId")
    Optional<LevelTest> findByTestIdAndUserId(@Param("testId") Long testId, @Param("userId") UUID userId);
}