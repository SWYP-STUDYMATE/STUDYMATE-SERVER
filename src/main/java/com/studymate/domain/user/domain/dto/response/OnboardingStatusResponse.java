package com.studymate.domain.user.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingStatusResponse {
    private boolean basicInfoCompleted;
    private boolean languageInfoCompleted;
    private boolean interestInfoCompleted;
    private boolean partnerInfoCompleted;
    private boolean scheduleInfoCompleted;
    private boolean onboardingCompleted;
    private int currentStep;
    private int totalSteps;

    // 새로운 UX 개선을 위한 필드들
    private double progressPercentage; // 0.0 ~ 100.0
    @JsonProperty("isCompleted")
    private boolean isCompleted;
    private Integer nextStep;
    private List<Integer> completedSteps;

    public OnboardingStatusResponse(boolean basicInfoCompleted,
                                   boolean languageInfoCompleted,
                                   boolean interestInfoCompleted,
                                   boolean partnerInfoCompleted,
                                   boolean scheduleInfoCompleted,
                                   boolean onboardingCompleted,
                                   int currentStep,
                                   int totalSteps) {
        this.basicInfoCompleted = basicInfoCompleted;
        this.languageInfoCompleted = languageInfoCompleted;
        this.interestInfoCompleted = interestInfoCompleted;
        this.partnerInfoCompleted = partnerInfoCompleted;
        this.scheduleInfoCompleted = scheduleInfoCompleted;
        this.onboardingCompleted = onboardingCompleted;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.progressPercentage = calculateProgress(currentStep, totalSteps);
        this.isCompleted = onboardingCompleted;
        this.nextStep = resolveNextStep(currentStep, totalSteps, onboardingCompleted);
        this.completedSteps = generateCompletedSteps(currentStep, totalSteps);
    }

    public OnboardingStatusResponse(boolean basicInfoCompleted,
                                   boolean languageInfoCompleted,
                                   boolean interestInfoCompleted,
                                   boolean partnerInfoCompleted,
                                   boolean scheduleInfoCompleted,
                                   boolean onboardingCompleted,
                                   int currentStep,
                                   int totalSteps,
                                   Integer nextStep) {
        this(basicInfoCompleted,
             languageInfoCompleted,
             interestInfoCompleted,
             partnerInfoCompleted,
             scheduleInfoCompleted,
             onboardingCompleted,
             currentStep,
             totalSteps);
        this.nextStep = nextStep != null
                ? Math.min(Math.max(nextStep, 1), Math.max(totalSteps, 1))
                : resolveNextStep(currentStep, totalSteps, onboardingCompleted);
        this.completedSteps = generateCompletedSteps(currentStep, totalSteps);
    }

    private double calculateProgress(int currentStep, int totalSteps) {
        if (totalSteps <= 0) {
            return 0.0;
        }
        double progress = ((double) currentStep / totalSteps) * 100.0;
        return Math.min(Math.max(progress, 0.0), 100.0);
    }

    private int resolveNextStep(int currentStep, int totalSteps, boolean onboardingCompleted) {
        if (onboardingCompleted) {
            return totalSteps > 0 ? totalSteps : 1;
        }

        int safeTotal = totalSteps > 0 ? totalSteps : 1;
        int candidate = currentStep + 1;
        if (candidate < 1) {
            candidate = 1;
        }
        if (candidate > safeTotal) {
            candidate = safeTotal;
        }
        return candidate;
    }

    public OnboardingStatusResponse withCompletedSteps(List<Integer> completedSteps) {
        this.completedSteps = completedSteps != null ? completedSteps : Collections.emptyList();
        return this;
    }

    public OnboardingStatusResponse withCompletedStepsFromMap(Map<String, Boolean> completedStepMap) {
        if (completedStepMap == null || completedStepMap.isEmpty()) {
            this.completedSteps = Collections.emptyList();
            return this;
        }

        this.completedSteps = completedStepMap.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(this::extractStepNumber)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        return this;
    }

    private List<Integer> generateCompletedSteps(int currentStep, int totalSteps) {
        if (currentStep <= 0) {
            return Collections.emptyList();
        }

        int safeTotal = totalSteps > 0 ? totalSteps : currentStep;
        int upperBound = Math.min(currentStep, safeTotal);
        return IntStream.rangeClosed(1, upperBound)
                .boxed()
                .collect(Collectors.toList());
    }

    private Integer extractStepNumber(String key) {
        if (key == null) {
            return null;
        }

        if (key.startsWith("step")) {
            try {
                return Integer.parseInt(key.substring(4));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
