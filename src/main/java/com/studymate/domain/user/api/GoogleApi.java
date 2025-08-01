package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.GoogleTokenResponse;
import com.studymate.domain.user.domain.dto.response.GoogleUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
