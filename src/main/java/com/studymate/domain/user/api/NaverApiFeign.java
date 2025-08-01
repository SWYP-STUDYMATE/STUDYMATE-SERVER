package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.NaverUserInfoWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "NaverApi", url = "https://openapi.naver.com", configuration = AuthFeignConfig.class)
public interface NaverApiFeign {
    @GetMapping ("/v1/nid/me")
    NaverUserInfoWrapper getUserInfo(@RequestHeader("Authorization") String token);
}
