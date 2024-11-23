package com.globalnest.be.oauth.dto.social;


import com.globalnest.be.user.domain.type.OAuthType;

import java.util.Map;

public record NaverResponse(Map<String, Object> attribute) implements OAuth2Response {

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = ((Map<String, Object>) attribute.get("response"));
    }

    @Override
    public OAuthType getProvider() {
        return OAuthType.NAVER;
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getProfileImage() {
        return attribute.get("profile_image").toString();
    }
}
