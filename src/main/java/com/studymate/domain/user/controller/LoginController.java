package com.studymate.domain.user.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class LoginController {
    private final LoginService loginService;

    @Value("${naver.client_id}")
    private String naverClientId;

    @Value("${naver.redirect_uri}")
    private String naverRedirectUri;

    @Value("${google.client_id}")
    private String googleClientId;

    @Value("${google.redirect_uri}")
    private String googleRedirectUri;

    @Value("${frontend.base_url:http://localhost:3000}")
    private String frontendBaseUrl;

    @GetMapping("/login/naver")
    public String naverLoginPage() {
        String state = UUID.randomUUID().toString();
        return loginService.getLoginUrl("naver", state, naverClientId, naverRedirectUri);
    }

    @GetMapping("/login/google")
    public String googleLoginPage() {
        String state = UUID.randomUUID().toString();
        return loginService.getLoginUrl("google", state, googleClientId, googleRedirectUri);
    }

    @GetMapping("/login/oauth2/code/naver")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("naver", code, state);

        // 디버깅 로그 추가
        System.out.println("네이버 로그인 - userId: " + tokens.userId());
        System.out.println("네이버 로그인 - accessToken: " + tokens.accessToken());

        // 2) FE 로그인 완료 페이지로 리다이렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl + "/main")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("userId", tokens.userId().toString())
                .build()
                .toUriString();

        System.out.println("네이버 리다이렉트 URL: " + redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/login/oauth2/code/google")
    public void googleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("google", code, state);

        // 디버깅 로그 추가
        System.out.println("구글 로그인 - userId: " + tokens.userId());
        System.out.println("구글 로그인 - accessToken: " + tokens.accessToken());

        // 2) FE 로그인 완료 페이지로 리다이렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl + "/main")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("userId", tokens.userId().toString())
                .build()
                .toUriString();

        System.out.println("구글 리다이렉트 URL: " + redirectUrl);

        response.sendRedirect(redirectUrl);
    }

}
