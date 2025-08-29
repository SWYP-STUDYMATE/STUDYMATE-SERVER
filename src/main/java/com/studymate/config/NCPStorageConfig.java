package com.studymate.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class NCPStorageConfig {
    @Value("${cloud.ncp.storage.region}")
    private String region;

    @Value("${cloud.ncp.storage.endpoint}")
    private String endPoint;

    @Value("${cloud.ncp.storage.access-key}")
    private String accessKey;

    @Value("${cloud.ncp.storage.secret-key}")
    private String secretKey;

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== NCP Storage Configuration Debug ===");
        
        if (region == null || region.trim().isEmpty()) {
            log.error("❌ CRITICAL: cloud.ncp.storage.region is NULL or EMPTY! Current value: '{}'", region);
        } else {
            log.info("✅ cloud.ncp.storage.region: {}", region);
        }
        
        if (endPoint == null || endPoint.trim().isEmpty()) {
            log.error("❌ CRITICAL: cloud.ncp.storage.endpoint is NULL or EMPTY! Current value: '{}'", endPoint);
        } else {
            log.info("✅ cloud.ncp.storage.endpoint: {}", endPoint);
        }
        
        if (accessKey == null || accessKey.trim().isEmpty()) {
            log.error("❌ CRITICAL: cloud.ncp.storage.access-key is NULL or EMPTY! Current value: '{}'", accessKey);
        } else {
            String maskedKey = accessKey.length() > 8 ? accessKey.substring(0, 8) + "***" : "***";
            log.info("✅ cloud.ncp.storage.access-key: {}", maskedKey);
        }
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("❌ CRITICAL: cloud.ncp.storage.secret-key is NULL or EMPTY! Current value: '{}'", secretKey);
        } else {
            String maskedKey = secretKey.length() > 8 ? secretKey.substring(0, 8) + "***" : "***";
            log.info("✅ cloud.ncp.storage.secret-key: {}", maskedKey);
        }
        
        log.info("=== NCP Storage Configuration Debug Complete ===");
    }

    @Bean
    public AmazonS3Client objectStorageClient() {
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint,region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey,secretKey)))
                .build();
    }


}
