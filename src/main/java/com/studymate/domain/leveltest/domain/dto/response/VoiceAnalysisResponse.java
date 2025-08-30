package com.studymate.domain.leveltest.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAnalysisResponse {
    
    private Long testId;
    private String transcriptText;
    private Integer overallScore;
    private String cefrLevel;
    
    // 세부 점수
    private ScoreBreakdown scoreBreakdown;
    
    // 분석 결과
    private AnalysisDetails analysisDetails;
    
    // 피드백 및 추천
    private String feedback;
    private String strengths;
    private String weaknesses;
    private List<String> recommendations;
    
    private LocalDateTime analyzedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdown {
        private Integer pronunciationScore;
        private Integer fluencyScore;
        private Integer grammarScore;
        private Integer vocabularyScore;
        private Map<String, Integer> detailedScores; // 추가 세부 점수
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisDetails {
        // 발음 분석
        private PronunciationAnalysis pronunciation;
        
        // 유창성 분석
        private FluencyAnalysis fluency;
        
        // 문법 분석
        private GrammarAnalysis grammar;
        
        // 어휘 분석
        private VocabularyAnalysis vocabulary;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PronunciationAnalysis {
        private Double accuracyPercentage;
        private List<String> mispronunciations;
        private List<String> strongPoints;
        private List<String> improvementAreas;
        private Map<String, Double> phoneticScores; // 음소별 점수
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FluencyAnalysis {
        private Double wordsPerMinute;
        private Double pauseFrequency;
        private Double hesitationCount;
        private Double continuityScore;
        private List<String> fluentPhrases;
        private List<String> hesitantPhrases;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarAnalysis {
        private Integer correctStructures;
        private Integer totalStructures;
        private List<GrammarError> errors;
        private List<String> wellUsedStructures;
        private String complexityLevel;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarError {
        private String errorType;
        private String originalText;
        private String correctedText;
        private String explanation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VocabularyAnalysis {
        private Integer uniqueWords;
        private Integer totalWords;
        private String lexicalDiversity;
        private List<String> advancedWords;
        private List<String> basicWords;
        private Map<String, Integer> categoryBreakdown; // 어휘 카테고리별 분석
    }
}