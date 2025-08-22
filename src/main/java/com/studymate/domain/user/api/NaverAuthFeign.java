package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "NaverAuth", url = "https://nid.naver.com",configuration = AuthFeignConfig.class)
public interface NaverAuthFeign {

    @PostMapping(value = "/oauth2.0/token", consumes = "application/x-www-form-urlencoded")
//    NaverTokenResponse getToken(@RequestBody Map<String,?> form);
    NaverTokenResponse getToken(@RequestParam Map<String, ?> form);
}
