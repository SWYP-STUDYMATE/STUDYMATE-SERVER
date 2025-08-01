package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.GoogleTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "GoogleAuth", url = "https://oauth2.googleapis.com" ,configuration = AuthFeignConfig.class)
public interface GoogleAuthFeign {

    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
    GoogleTokenResponse getToken (@RequestBody Map<String,?> form);

}
