package com.globalnest.be.oauth.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secretKey,
    String issuer,
    Long accessTokenExpiration,
    Long refreshTokenExpiration) {

}