package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Legacy OAuth Controller - 구버전 경로 임시 지원
 * 배포 완료 후 제거 예정
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LegacyOAuthController {
    
    private final LoginService loginService;

    @GetMapping("/login/oauth2/code/naver")
    public void legacyNaverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws IOException {
        log.info("Legacy OAuth callback accessed - should be removed after deployment");
        
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("naver", code, state);

        // 2) FE OAuth 콜백 페이지로 리다이렉트 (토큰과 함께)
        String redirectUrl = UriComponentsBuilder
                .fromUriString("https://languagemate.kr/login/oauth2/code/naver")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/login/oauth2/code/google")
    public void legacyGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    ) throws IOException {
        log.info("Legacy OAuth callback accessed - should be removed after deployment");
        
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("google", code, state);

        // 2) FE OAuth 콜백 페이지로 리다이렉트 (토큰과 함께)
        String redirectUrl = UriComponentsBuilder
                .fromUriString("https://languagemate.kr/login/oauth2/code/google")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}