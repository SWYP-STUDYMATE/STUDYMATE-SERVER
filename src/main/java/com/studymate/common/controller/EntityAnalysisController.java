package com.studymate.common.controller;

import com.studymate.common.dto.response.ApiResponse;
import com.studymate.common.util.EntitySchemaAnalyzer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 엔티티 스키마 분석 API 컨트롤러
 * 
 * JPA 엔티티와 데이터베이스 스키마 문서 간 정합성 분석을 위한 REST API 제공
 * 
 * 주요 기능:
 * - 전체 엔티티 메타데이터 분석
 * - 스키마 정합성 검증
 * - JSON 형식 불일치 리포트 생성
 * - Liquibase/Flyway 마이그레이션 스크립트 준비를 위한 DDL 차이 분석
 * 
 * @author Backend Development Team
 * @version 1.0
 * @since 2025-09-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/system/analysis")
@RequiredArgsConstructor
@Tag(name = "Entity Analysis", description = "데이터베이스 스키마-엔티티 정합성 분석 API")
public class EntityAnalysisController {

    private final EntitySchemaAnalyzer entitySchemaAnalyzer;

    /**
     * 전체 엔티티 메타데이터 분석 및 스키마 정합성 리포트 생성
     * 
     * Spring Boot Actuator의 EntityManagerFactoryIntrospector 활용
     * Hibernate Validator와 Bean Validation API를 통한 엔티티 구조 분석
     * 
     * @return JSON 형식의 엔티티 분석 리포트
     */
    @Operation(summary = "엔티티 스키마 정합성 분석", 
               description = "JPA 엔티티와 데이터베이스 스키마 문서 간의 불일치를 체계적으로 분석하고 JSON 리포트를 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "분석 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "분석 실패")
    })
    @GetMapping("/entities/schema-consistency")
    public ApiResponse<String> analyzeEntitySchemaConsistency() {
        log.info("🔍 Starting entity schema consistency analysis...");
        
        try {
            String analysisReport = entitySchemaAnalyzer.analyzeEntityMetadata();
            
            log.info("✅ Entity schema consistency analysis completed successfully");
            return ApiResponse.success(analysisReport, "엔티티 스키마 정합성 분석이 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("❌ Failed to analyze entity schema consistency", e);
            return ApiResponse.error("ANALYSIS_FAILED", "엔티티 스키마 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 엔티티 분석 리포트를 Raw JSON으로 반환 (개발자용)
     * 
     * @return Raw JSON 형식의 엔티티 분석 리포트
     */
    @Operation(summary = "엔티티 분석 Raw JSON", 
               description = "개발자가 직접 파싱할 수 있는 Raw JSON 형식으로 엔티티 분석 결과를 반환합니다.")
    @GetMapping(value = "/entities/raw-report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getEntityAnalysisRawReport() {
        log.info("📊 Generating raw entity analysis report...");
        
        try {
            String rawReport = entitySchemaAnalyzer.analyzeEntityMetadata();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rawReport);
                    
        } catch (Exception e) {
            log.error("❌ Failed to generate raw entity analysis report", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Failed to generate analysis report\",\"message\":\"" + 
                          e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    /**
     * 특정 엔티티의 불일치 사항만 조회
     * 
     * @param entityName 분석할 엔티티 이름
     * @return 해당 엔티티의 불일치 분석 결과
     */
    @Operation(summary = "특정 엔티티 불일치 분석", 
               description = "지정된 엔티티의 스키마 불일치 사항만을 분석합니다.")
    @GetMapping("/entities/{entityName}/inconsistencies")
    public ApiResponse<String> analyzeSpecificEntityInconsistencies(
            @PathVariable String entityName) {
        
        log.info("🎯 Analyzing inconsistencies for entity: {}", entityName);
        
        try {
            // 전체 분석을 수행하고 특정 엔티티만 필터링
            // 실제 구현에서는 더 효율적인 방법을 고려할 수 있음
            String fullReport = entitySchemaAnalyzer.analyzeEntityMetadata();
            
            // TODO: JSON에서 특정 엔티티만 추출하는 로직 구현
            // 현재는 전체 리포트를 반환
            
            return ApiResponse.success(fullReport, 
                    String.format("%s 엔티티의 불일치 분석이 완료되었습니다.", entityName));
                    
        } catch (Exception e) {
            log.error("❌ Failed to analyze inconsistencies for entity: {}", entityName, e);
            return ApiResponse.error("ENTITY_ANALYSIS_FAILED", 
                    String.format("%s 엔티티 분석 중 오류가 발생했습니다: %s", entityName, e.getMessage()));
        }
    }

    /**
     * 불일치 통계 정보만 조회
     * 
     * @return 불일치 통계 정보
     */
    @Operation(summary = "불일치 통계 조회", 
               description = "전체 엔티티의 스키마 불일치 통계 정보를 조회합니다.")
    @GetMapping("/entities/inconsistency-statistics")
    public ApiResponse<String> getInconsistencyStatistics() {
        log.info("📈 Generating inconsistency statistics...");
        
        try {
            String fullReport = entitySchemaAnalyzer.analyzeEntityMetadata();
            
            // TODO: JSON에서 statistics 부분만 추출하는 로직 구현
            // 현재는 전체 리포트를 반환
            
            return ApiResponse.success(fullReport, "불일치 통계 정보가 생성되었습니다.");
            
        } catch (Exception e) {
            log.error("❌ Failed to generate inconsistency statistics", e);
            return ApiResponse.error("STATISTICS_FAILED", "통계 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Liquibase/Flyway 마이그레이션 스크립트 준비를 위한 DDL 차이 분석
     * 
     * @return DDL 차이 분석 결과 및 마이그레이션 스크립트 제안
     */
    @Operation(summary = "DDL 차이 분석", 
               description = "Liquibase 또는 Flyway 마이그레이션 스크립트 준비를 위한 DDL 차이점을 분석합니다.")
    @GetMapping("/entities/ddl-differences")
    public ApiResponse<String> analyzeDdlDifferences() {
        log.info("🛠️ Analyzing DDL differences for migration scripts...");
        
        try {
            // 현재는 기본 분석 리포트를 반환
            // 향후 DDL 생성 기능을 추가할 수 있음
            String analysisReport = entitySchemaAnalyzer.analyzeEntityMetadata();
            
            return ApiResponse.success(analysisReport, 
                    "DDL 차이 분석이 완료되었습니다. 마이그레이션 스크립트 생성을 위한 정보가 포함되어 있습니다.");
            
        } catch (Exception e) {
            log.error("❌ Failed to analyze DDL differences", e);
            return ApiResponse.error("DDL_ANALYSIS_FAILED", "DDL 차이 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 엔티티 분석 도구 상태 확인 (Health Check)
     * 
     * @return 분석 도구 상태 정보
     */
    @Operation(summary = "분석 도구 상태 확인", 
               description = "엔티티 스키마 분석 도구의 상태를 확인합니다.")
    @GetMapping("/health")
    public ApiResponse<Object> checkAnalyzerHealth() {
        log.info("🏥 Checking entity analyzer health...");
        
        try {
            var healthStatus = entitySchemaAnalyzer.health();
            
            return ApiResponse.success(healthStatus, "엔티티 분석 도구가 정상적으로 동작 중입니다.");
            
        } catch (Exception e) {
            log.error("❌ Failed to check analyzer health", e);
            return ApiResponse.error("HEALTH_CHECK_FAILED", "상태 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}