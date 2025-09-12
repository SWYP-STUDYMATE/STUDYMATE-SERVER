package com.studymate.domain.user.controller;

import com.studymate.common.dto.response.ApiResponse;
import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.service.LoginService;
import com.studymate.domain.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refreshTokens(
            @RequestHeader("Authorization") String authorization
    ) {
        String refreshToken = authorization.replace("Bearer ", "");
        log.info("refreshToken");
        TokenResponse newTokens = tokenService.refreshToken(refreshToken);
        return ApiResponse.success(newTokens, "토큰이 갱신되었습니다.");
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ","");
        tokenService.logout(accessToken);
        return ApiResponse.success("로그아웃되었습니다.");
    }
}
