package com.sportlink.account.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sportlink.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long expirationMinutes = 720;
    private String issuer = "sportlink-account-management";
}
