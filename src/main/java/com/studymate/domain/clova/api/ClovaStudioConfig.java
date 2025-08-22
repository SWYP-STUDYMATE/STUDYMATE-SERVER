package com.studymate.domain.clova.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "clova.studio")
@Data
public class ClovaStudioConfig {
    private String apiKey;
    private String endpoint;
    private String model;
}
