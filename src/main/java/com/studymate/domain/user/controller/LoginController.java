package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

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



    @GetMapping("api/v1/login/naver")
    public void naverLoginPage(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String loginUrl = loginService.getLoginUrl("naver",state, naverClientId, naverRedirectUri);
        response.sendRedirect(loginUrl);
    }

    @GetMapping("api/v1/login/google")
    public void googleLoginPage(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String loginUrl = loginService.getLoginUrl("google",state, googleClientId, googleRedirectUri);
        response.sendRedirect(loginUrl);
    }


    @GetMapping("/login/oauth2/code/naver")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("naver",code, state);

        // 2) FE OAuth 콜백 페이지로 리다이렉트 (토큰과 함께)
        String redirectUrl = UriComponentsBuilder
//                .fromUriString("http://localhost:3000/login/oauth2/code/naver")
                .fromUriString("https://languagemate.kr/login/oauth2/code/naver")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
    @GetMapping("/login/oauth2/code/google")
    public void googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    ) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("google",code,state);

        // 2) FE OAuth 콜백 페이지로 리다이렉트 (토큰과 함께)
        String redirectUrl = UriComponentsBuilder
//                .fromUriString("http://localhost:3000/login/oauth2/code/google")
                .fromUriString("https://languagemate.kr/login/oauth2/code/google")
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

}
