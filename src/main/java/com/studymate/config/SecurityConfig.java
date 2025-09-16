package com.studymate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.util.JwtAuthenticationFilter;
import com.studymate.auth.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtUtils jwtUtils,
                        UserDao userDao) throws Exception {
                http
                                .cors(withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .formLogin(AbstractHttpConfigurer::disable)
                                .anonymous(withDefaults())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((req, res, authEx) -> res.sendError(
                                                                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                                .authorizeHttpRequests(authz -> authz
                                                // 1) OPTIONS, 로그인/토큰 엔드포인트
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/v1/login/**", "/api/v1/login/**", "/v1/auth/**", "/api/v1/auth/**").permitAll()
                                                .requestMatchers("/health", "/v1/health", "/api/v1/health", "/actuator/health").permitAll()
                                        // OAuth2 콜백 경로 허용
                                        .requestMatchers("/login/oauth2/code/**", "/v1/login/oauth2/code/**", "/api/v1/login/oauth2/code/**").permitAll()


                                                // 인증 없이 접근 가능한 옵션 조회용 API
                                                .requestMatchers("/v1/onboarding/interest/motivations", "/api/v1/onboarding/interest/motivations",
                                                                "/v1/onboarding/interest/topics", "/api/v1/onboarding/interest/topics",
                                                                "/v1/onboarding/interest/learning-styles", "/api/v1/onboarding/interest/learning-styles",
                                                                "/v1/onboarding/interest/learning-expectations", "/api/v1/onboarding/interest/learning-expectations",
                                                                "/v1/onboarding/language/languages", "/api/v1/onboarding/language/languages",
                                                                "/v1/onboarding/language/level-types-language", "/api/v1/onboarding/language/level-types-language",
                                                                "/v1/onboarding/language/level-types-partner", "/api/v1/onboarding/language/level-types-partner",
                                                                "/v1/onboarding/partner/gender-type", "/api/v1/onboarding/partner/gender-type",
                                                                "/v1/onboarding/partner/personalities", "/api/v1/onboarding/partner/personalities",
                                                                "/v1/onboarding/schedule/communication-methods", "/api/v1/onboarding/schedule/communication-methods",
                                                                "/v1/onboarding/schedule/daily-methods", "/api/v1/onboarding/schedule/daily-methods",
                                                                "/v1/onboarding/schedule/group-sizes", "/api/v1/onboarding/schedule/group-sizes",
                                                                "/v1/user/locations", "/api/v1/user/locations",
                                                                "/v1/user/gender-type", "/api/v1/user/gender-type",
                                                                // System Analysis 엔드포인트 (개발/분석용)
                                                                "/api/v1/system/analysis/**")
                                                .permitAll()
                                                // 2) SockJS/WebSocket 핸드셰이크 경로
                                                .requestMatchers("/ws/**").permitAll()
                                                // 3) 나머지 API
                                                .anyRequest().authenticated())

                                // JWT 필터는 WebSocket 핸드셰이크 이후 CONNECT 프레임 처리용
                                .addFilterBefore(
                                                new JwtAuthenticationFilter(jwtUtils, userDao),
                                                UsernamePasswordAuthenticationFilter.class);


                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("https://languagemate.kr", "http://localhost:3000"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}