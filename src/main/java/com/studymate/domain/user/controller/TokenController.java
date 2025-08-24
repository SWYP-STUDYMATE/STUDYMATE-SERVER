package com.studymate.domain.user.controller;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import com.studymate.domain.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refreshTokens(
            @RequestHeader("Authorization") String authorization
    ) {
        String refreshToken = authorization.replace("Bearer ", "");
        TokenResponse newTokens = tokenService.refreshToken(refreshToken);
        return ResponseEntity.ok(newTokens);
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ","");
        tokenService.logout(accessToken);
    }
}
