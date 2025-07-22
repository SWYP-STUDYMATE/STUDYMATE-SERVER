package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
