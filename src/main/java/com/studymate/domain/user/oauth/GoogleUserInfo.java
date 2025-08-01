package com.studymate.domain.user.oauth;

import com.studymate.domain.user.domain.dto.response.GoogleUserInfoResponse;

public class GoogleUserInfo implements OAuthUserInfo{
    private final GoogleUserInfoResponse res;

    public GoogleUserInfo (GoogleUserInfoResponse res) {
        this.res = res;
    }
    @Override
    public String getId() {
        return res.sub();
    }

    @Override
    public String getName() {
        return res.name();
    }
}
