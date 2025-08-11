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

import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.util.JwtAuthenticationFilter;
import com.studymate.domain.user.util.JwtUtils;

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
                                                .requestMatchers("/login/**", "/auth/**").permitAll()
                                                .requestMatchers("/health").permitAll()
                                                // 인증 없이 접근 가능한 옵션 조회용 API
                                                .requestMatchers("/onboard/interest/motivations",
                                                                "/onboard/interest/topics",
                                                                "/onboard/interest/learning-styles",
                                                                "/onboard/interest/learning-expectations",
                                                                "/onboard/language/languages",
                                                                "/onboard/language/level-types-language",
                                                                "/onboard/language/level-types-partner",
                                                                "/onboard/partner/gender-type",
                                                                "/onboard/partner/personalities",
                                                                "/onboard/schedule/communication-methods",
                                                                "/onboard/schedule/daily-methods",
                                                                "/onboard/schedule/group-sizes", "/user/locations",
                                                                "/user/gender-type")
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
}