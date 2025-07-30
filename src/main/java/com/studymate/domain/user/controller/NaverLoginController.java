package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class NaverLoginController {
    private final LoginService naverLoginService;

    @Value("${naver.client_id}")
    private String clientId;

    @Value("${naver.client_secret}")
    private String clientSecret;

    @Value("${naver.redirect_uri}")
    private String redirectUri;

    @GetMapping("/login/naver")
    public String loginPage() {
        String state = UUID.randomUUID().toString();
        return naverLoginService.getLoginUrl(state, clientId, redirectUri);
    }

    @GetMapping("/login/oauth2/code/naver")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws IOException {
        // 1) 토큰 발급
        TokenResponse tokens = naverLoginService.getLoginTokenCallback(code, state);

        // 2) FE 로그인 완료 페이지로 리다이렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/main")
                .queryParam("accessToken", tokens.accessToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
