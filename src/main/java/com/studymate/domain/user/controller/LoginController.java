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
    public String naverLoginPage() {
        String state = UUID.randomUUID().toString();
        return loginService.getLoginUrl("naver",state, naverClientId, naverRedirectUri);
    }

    @GetMapping("api/v1/login/google")
    public String googleLoginPage() {
        String state = UUID.randomUUID().toString();
        return loginService.getLoginUrl("google",state, googleClientId, googleRedirectUri);
    }
swit


    @GetMapping("/login/oauth2/code/naver")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("naver",code, state);

        // 2) FE 로그인 완료 페이지로 리다이렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/main")
//                .fromUriString("https://languagemate.kr/main")
                .queryParam("accessToken", tokens.accessToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
    @GetMapping("/login/oauth2/code/google")
    public void googleCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = loginService.getLoginTokenCallback("google",code,null);

        // 2) FE 로그인 완료 페이지로 리다이렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/main")
//                .fromUriString("https://languagemate.kr/main")
                .queryParam("accessToken", tokens.accessToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

}
