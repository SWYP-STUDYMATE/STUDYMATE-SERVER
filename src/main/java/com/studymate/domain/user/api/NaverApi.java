package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApi {
    private final NaverAuthFeign naverAuthFeign;
    private final NaverApiFeign naverApiFeign;

    @Value("${naver.client_id}")
    private String clientId;

    @Value("${naver.client_secret}")
    private String clientSecret;

    @Value("${naver.redirect_uri}")
    private String redirectUri;

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== Naver OAuth Configuration Debug ===");
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("❌ CRITICAL: naver.client_id is NULL or EMPTY! Current value: '{}'", clientId);
        } else {
            String maskedId = clientId.length() > 8 ? clientId.substring(0, 8) + "***" : "***";
            log.info("✅ naver.client_id: {}", maskedId);
        }
        
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.error("❌ CRITICAL: naver.client_secret is NULL or EMPTY! Current value: '{}'", clientSecret);
        } else {
            log.info("✅ naver.client_secret: *** (masked)");
        }
        
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            log.error("❌ CRITICAL: naver.redirect_uri is NULL or EMPTY! Current value: '{}'", redirectUri);
        } else {
            log.info("✅ naver.redirect_uri: {}", redirectUri);
        }
        
        log.info("=== Naver OAuth Configuration Debug Complete ===");
    }

    public NaverTokenResponse getToken(String code, String state) {
        Map<String,String> form = new HashMap<>();
        form.put("grant_type","authorization_code");
        form.put("client_id",clientId);
        form.put("client_secret",clientSecret);
        form.put("redirect_uri",redirectUri);
        form.put("state",state);
        form.put("code",code);
        return naverAuthFeign.getToken(form);
    }

    public NaverUserInfoResponse getUserInfo(String accessToken){
        String bearerToken = "Bearer " + accessToken;
        NaverUserInfoWrapper wrapper = naverApiFeign.getUserInfo(bearerToken);
        return wrapper.response();
    }

}
