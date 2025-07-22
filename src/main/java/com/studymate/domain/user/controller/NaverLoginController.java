package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public TokenResponse callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ){
        return naverLoginService.getLoginTokenCallback(code, state);
    }







}
