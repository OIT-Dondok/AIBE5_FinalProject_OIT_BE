package com.oit.dondok.infrastructure.auth.token;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtTokenProperties(
    String issuer,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration,
    String secret) {}
