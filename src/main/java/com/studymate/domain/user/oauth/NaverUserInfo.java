package com.studymate.domain.user.oauth;

import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;

public class NaverUserInfo implements OAuthUserInfo{
    private final NaverUserInfoResponse res;

    public NaverUserInfo (NaverUserInfoResponse res) {
        this.res = res;
    }
    @Override
    public String getId() {
        return res.id();
    }

    @Override
    public String getName() {
        return res.name();
    }

    @Override
    public String getProfileImageUrl() {
        return res.profile_image();
    }
}

