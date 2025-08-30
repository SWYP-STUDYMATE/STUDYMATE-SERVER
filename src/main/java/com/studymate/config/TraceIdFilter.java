package com.studymate.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class TraceIdFilter implements Filter {
    
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 헤더에서 Trace ID 추출하거나 새로 생성
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        try {
            // MDC에 Trace ID 설정
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            
            // 요청 시작 로그
            log.info("Request started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            
            long startTime = System.currentTimeMillis();
            
            // 다음 필터 체인 실행
            chain.doFilter(request, response);
            
            // 요청 완료 로그
            long duration = System.currentTimeMillis() - startTime;
            log.info("Request completed: {} {} - {}ms", 
                httpRequest.getMethod(), httpRequest.getRequestURI(), duration);
                
        } finally {
            // MDC 정리
            MDC.clear();
        }
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}