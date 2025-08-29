package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.GoogleTokenResponse;
import com.studymate.domain.user.domain.dto.response.GoogleUserInfoResponse;
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
public class GoogleApi {
    private final GoogleApiFeign googleApiFeign;
    private final GoogleAuthFeign googleAuthFeign;
    @Value("${google.client_id}")
    private String clientId;

    @Value("${google.client_secret}")
    private String clientSecret;

    @Value("${google.redirect_uri}")
    private String redirectUri;

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== Google OAuth Configuration Debug ===");
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("❌ CRITICAL: google.client_id is NULL or EMPTY! Current value: '{}'", clientId);
        } else {
            String maskedId = clientId.length() > 12 ? clientId.substring(0, 12) + "***" : "***";
            log.info("✅ google.client_id: {}", maskedId);
        }
        
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.error("❌ CRITICAL: google.client_secret is NULL or EMPTY! Current value: '{}'", clientSecret);
        } else {
            log.info("✅ google.client_secret: *** (masked)");
        }
        
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            log.error("❌ CRITICAL: google.redirect_uri is NULL or EMPTY! Current value: '{}'", redirectUri);
        } else {
            log.info("✅ google.redirect_uri: {}", redirectUri);
        }
        
        log.info("=== Google OAuth Configuration Debug Complete ===");
    }

    public GoogleTokenResponse getToken (String code) {
        Map<String,String> form = new HashMap<>();
        form.put("code",code);
        form.put("client_id",clientId);
        form.put("client_secret",clientSecret);
        form.put("redirect_uri",redirectUri);
        form.put("grant_type","authorization_code");
        return googleAuthFeign.getToken(form);
    }

    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        String bearerToken = "Bearer " + accessToken;
        return googleApiFeign.getUserInfo(bearerToken);
    }

}
