package com.oit.dondok.domain.auth.service;

import com.oit.dondok.domain.auth.exception.AuthErrorCode;
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
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "typ";
  private static final String ACCESS_TOKEN_TYPE = "ACCESS";
  private static final String REFRESH_TOKEN_TYPE = "REFRESH";

  private final JwtTokenProperties jwtTokenProperties;

  /** 회원 UUID를 subject로 갖는 access token을 생성한다. */
  public String createAccessToken(UUID memberUuid) {
    return createToken(memberUuid, ACCESS_TOKEN_TYPE, jwtTokenProperties.accessTokenExpiration());
  }

  /** 회원 UUID를 subject로 갖는 refresh token을 생성한다. */
  public String createRefreshToken(UUID memberUuid) {
    return createToken(memberUuid, REFRESH_TOKEN_TYPE, jwtTokenProperties.refreshTokenExpiration());
  }

  /** access token의 서명, issuer, 만료 시간, token type을 검증하고 payload를 반환한다. */
  public TokenPayload parseAccessToken(String token) {
    return parseToken(token, ACCESS_TOKEN_TYPE);
  }

  /** refresh token의 서명, issuer, 만료 시간, token type을 검증하고 payload를 반환한다. */
  public TokenPayload parseRefreshToken(String token) {
    return parseToken(token, REFRESH_TOKEN_TYPE);
  }

  /** 공통 claim과 token type을 포함한 JWT 문자열을 생성한다. */
  private String createToken(UUID memberUuid, String tokenType, Duration expiration) {
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

  /** JWT를 파싱하면서 서명, issuer, 만료 시간, 기대한 token type을 검증한다. */
  private TokenPayload parseToken(String token, String expectedTokenType) {
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

  /** token의 typ claim이 기대한 token type과 일치하는지 확인한다. */
  private void validateTokenType(String tokenType, String expectedTokenType) {
    if (!expectedTokenType.equals(tokenType)) {
      throw invalidTokenException(expectedTokenType, null);
    }
  }

  /** 설정된 secret 문자열로 HMAC 서명 키를 만든다. */
  private SecretKey secretKey() {
    return Keys.hmacShaKeyFor(jwtTokenProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

  /** 만료된 token type에 맞는 CustomException을 생성한다. */
  private CustomException expiredTokenException(String tokenType, Throwable cause) {
    if (ACCESS_TOKEN_TYPE.equals(tokenType)) {
      return new CustomException(AuthErrorCode.ACCESS_TOKEN_EXPIRED, cause);
    }
    return new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED, cause);
  }

  /** 유효하지 않은 token type에 맞는 CustomException을 생성한다. */
  private CustomException invalidTokenException(String tokenType, Throwable cause) {
    if (ACCESS_TOKEN_TYPE.equals(tokenType)) {
      return new CustomException(AuthErrorCode.ACCESS_TOKEN_INVALID, cause);
    }
    return new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID, cause);
  }
}
