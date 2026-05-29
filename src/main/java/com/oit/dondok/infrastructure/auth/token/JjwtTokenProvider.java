package com.oit.dondok.infrastructure.auth.token;

import com.oit.dondok.domain.auth.exception.AuthErrorCode;
import com.oit.dondok.domain.auth.service.TokenPayload;
import com.oit.dondok.domain.auth.service.TokenProvider;
import com.oit.dondok.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JjwtTokenProvider implements TokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "typ";
  private static final String ACCESS_TOKEN_TYPE = "ACCESS";
  private static final String REFRESH_TOKEN_TYPE = "REFRESH";

  private final JwtTokenProperties jwtTokenProperties;

  @Override
  public String createAccessToken(UUID memberUuid) {
    return createToken(memberUuid, ACCESS_TOKEN_TYPE, jwtTokenProperties.accessTokenExpiration());
  }

  @Override
  public String createRefreshToken(UUID memberUuid) {
    return createToken(memberUuid, REFRESH_TOKEN_TYPE, jwtTokenProperties.refreshTokenExpiration());
  }

  @Override
  public TokenPayload parseAccessToken(String token) {
    return parseToken(token, ACCESS_TOKEN_TYPE);
  }

  @Override
  public TokenPayload parseRefreshToken(String token) {
    return parseToken(token, REFRESH_TOKEN_TYPE);
  }

  private String createToken(UUID memberUuid, String tokenType, Duration expiration) {
    Objects.requireNonNull(memberUuid, "memberUuid must not be null");

    Instant now = Instant.now();
    Instant expiresAt = now.plus(expiration);

    return Jwts.builder()
        .issuer(jwtTokenProperties.issuer())
        .subject(memberUuid.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .signWith(secretKey())
        .compact();
  }

  private TokenPayload parseToken(String token, String expectedTokenType) {
    if (token == null || token.isBlank()) {
      throw invalidTokenException(expectedTokenType, null);
    }

    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(secretKey())
              .requireIssuer(jwtTokenProperties.issuer())
              .build()
              .parseSignedClaims(token)
              .getPayload();

      String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
      validateTokenType(tokenType, expectedTokenType);

      return new TokenPayload(
          UUID.fromString(claims.getSubject()),
          tokenType,
          claims.getIssuedAt().toInstant(),
          claims.getExpiration().toInstant());
    } catch (ExpiredJwtException exception) {
      throw expiredTokenException(expectedTokenType, exception);
    } catch (JwtException | IllegalArgumentException exception) {
      throw invalidTokenException(expectedTokenType, exception);
    }
  }

  private void validateTokenType(String tokenType, String expectedTokenType) {
    if (!expectedTokenType.equals(tokenType)) {
      throw invalidTokenException(expectedTokenType, null);
    }
  }

  private SecretKey secretKey() {
    return Keys.hmacShaKeyFor(jwtTokenProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

  private CustomException expiredTokenException(String tokenType, Throwable cause) {
    if (ACCESS_TOKEN_TYPE.equals(tokenType)) {
      return new CustomException(AuthErrorCode.ACCESS_TOKEN_EXPIRED, cause);
    }
    return new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED, cause);
  }

  private CustomException invalidTokenException(String tokenType, Throwable cause) {
    if (ACCESS_TOKEN_TYPE.equals(tokenType)) {
      return new CustomException(AuthErrorCode.ACCESS_TOKEN_INVALID, cause);
    }
    return new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID, cause);
  }
}
