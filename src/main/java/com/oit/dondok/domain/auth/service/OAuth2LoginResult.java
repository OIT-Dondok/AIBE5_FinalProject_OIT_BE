package com.oit.dondok.domain.auth.service;

import java.util.UUID;

public record OAuth2LoginResult(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenMaxAge,
    UUID memberUuid,
    String email,
    String nickname) {}
