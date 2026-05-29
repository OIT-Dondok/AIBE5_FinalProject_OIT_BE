package com.oit.dondok.domain.auth.service;

import java.time.Instant;
import java.util.UUID;

public record TokenPayload(
    UUID memberUuid, String tokenType, Instant issuedAt, Instant expiresAt) {}
