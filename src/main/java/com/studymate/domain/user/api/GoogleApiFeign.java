package com.studymate.domain.user.api;

import com.studymate.domain.user.domain.dto.response.GoogleUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;



@FeignClient(name = "GoolgleApi" ,url = "https://www.googleapis.com")
public interface GoogleApiFeign {

    @GetMapping("/oauth2/v3/userinfo")
    GoogleUserInfoResponse getUserInfo(@RequestHeader ("Authorization") String token);

}


