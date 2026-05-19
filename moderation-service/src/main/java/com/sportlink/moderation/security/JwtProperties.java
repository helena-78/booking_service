package com.sportlink.moderation.security;

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
    private String issuer = "sportlink-account-management";
}
