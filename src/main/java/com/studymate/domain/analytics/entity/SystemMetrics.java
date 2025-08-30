package com.studymate.domain.analytics.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemMetrics extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName; // DAILY_ACTIVE_USERS, SESSION_COUNT, MESSAGE_COUNT, etc.

    @Column(name = "metric_category", nullable = false, length = 50)
    private String metricCategory; // USER, SESSION, PERFORMANCE, BUSINESS, etc.

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(name = "metric_unit", length = 20)
    private String metricUnit; // COUNT, PERCENTAGE, MINUTES, SECONDS, etc.

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "aggregation_period", length = 20)
    private String aggregationPeriod; // HOURLY, DAILY, WEEKLY, MONTHLY

    @Column(name = "tags", length = 500)
    private String tags; // 추가 분류 태그

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public SystemMetrics(String metricName, String metricCategory, Double metricValue,
                        String metricUnit, LocalDateTime date, String aggregationPeriod,
                        String tags, String description) {
        this.metricName = metricName;
        this.metricCategory = metricCategory;
        this.metricValue = metricValue;
        this.metricUnit = metricUnit;
        this.date = date;
        this.aggregationPeriod = aggregationPeriod;
        this.tags = tags;
        this.description = description;
    }

    public void updateValue(Double newValue) {
        this.metricValue = newValue;
    }
}