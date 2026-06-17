package com.oit.dondok.domain.auth.service;

import com.oit.dondok.domain.auth.exception.AuthErrorCode;
import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.domain.member.entity.MemberStatus;
import com.oit.dondok.domain.member.repository.MemberRepository;
import com.oit.dondok.global.exception.CustomException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

  private static final int NICKNAME_MAX_LENGTH = 50;

  private final MemberRepository memberRepository;
  private final AuthService authService;
  private final PlatformTransactionManager transactionManager;

  /** Google 사용자 정보로 회원을 조회하거나 생성한 뒤 서비스 로그인 토큰을 발급한다. */
  @Transactional
  public OAuth2LoginResult login(OAuthUserInfo userInfo) {
    validateUserInfo(userInfo);

    Member member = resolveMember(userInfo);
    validateActive(member);

    LoginResult loginResult = authService.issueLoginToken(member);
    return new OAuth2LoginResult(
        loginResult.accessToken(),
        loginResult.accessTokenExpiresIn(),
        loginResult.refreshToken(),
        loginResult.refreshTokenMaxAge(),
        loginResult.memberUuid(),
        loginResult.email(),
        loginResult.nickname());
  }

  /** OAuth 고유 식별자와 이메일을 기준으로 로그인 대상 회원을 결정한다. */
  private Member resolveMember(OAuthUserInfo userInfo) {
    return memberRepository
        .findByOauthProviderAndOauthProviderId(userInfo.provider(), userInfo.providerId())
        .orElseGet(() -> resolveByEmailOrCreate(userInfo));
  }

  /** 같은 이메일 회원이 있으면 OAuth를 연결하고, 없으면 새 OAuth 회원을 생성한다. */
  private Member resolveByEmailOrCreate(OAuthUserInfo userInfo) {
    return memberRepository
        .findByEmail(normalizeEmail(userInfo.email()))
        .map(member -> connectOAuth(member, userInfo))
        .orElseGet(() -> createOAuthMember(userInfo));
  }

  /** 기존 회원에 Google OAuth 계정을 연결한다. */
  private Member connectOAuth(Member member, OAuthUserInfo userInfo) {
    if (member.hasDifferentOAuthAccount(userInfo.provider(), userInfo.providerId())) {
      throw new CustomException(AuthErrorCode.OAUTH_ACCOUNT_CONFLICT);
    }

    member.connectOAuth(userInfo.provider(), userInfo.providerId());
    return member;
  }

  /** 신규 Google OAuth 회원을 생성한다. */
  private Member createOAuthMember(OAuthUserInfo userInfo) {
    DataIntegrityViolationException lastException = null;

    for (int attempt = 0; attempt < 5; attempt++) {
      String nickname =
          attempt == 0 ? createAvailableNickname(userInfo.name()) : createRandomNickname();

      try {
        return saveOAuthMemberInNewTransaction(userInfo, nickname);
      } catch (DataIntegrityViolationException exception) {
        lastException = exception;
        Member resolvedMember = resolveMemberAfterCreateConflict(userInfo);
        if (resolvedMember != null) {
          return resolvedMember;
        }
      }
    }

    throw Objects.requireNonNull(lastException, "lastException must not be null");
  }

  /** Google 이메일과 인증 여부를 검증한다. */
  private void validateUserInfo(OAuthUserInfo userInfo) {
    if (userInfo == null
        || userInfo.provider() == null
        || userInfo.providerId() == null
        || userInfo.providerId().isBlank()
        || userInfo.email() == null
        || userInfo.email().isBlank()
        || !userInfo.emailVerified()) {
      throw new CustomException(AuthErrorCode.OAUTH_EMAIL_NOT_VERIFIED);
    }
  }

  /** 비활성화된 회원의 OAuth 로그인을 차단한다. */
  private void validateActive(Member member) {
    if (member.getStatus() == MemberStatus.DEACTIVATED) {
      throw new CustomException(AuthErrorCode.MEMBER_DEACTIVATED);
    }
  }

  /** 이메일을 저장/조회 규칙에 맞게 정규화한다. */
  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  /** 중복되지 않는 기본 닉네임을 생성한다. */
  private String createAvailableNickname(String name) {
    String base = normalizeNickname(name);
    if (!memberRepository.existsByNickname(base)) {
      return base;
    }

    for (int suffix = 1; suffix <= 9999; suffix++) {
      String candidate =
          truncateNickname(base, NICKNAME_MAX_LENGTH - 4) + String.format("%04d", suffix);
      if (!memberRepository.existsByNickname(candidate)) {
        return candidate;
      }
    }

    return "user_" + shortUuid();
  }

  /** 랜덤 기반 닉네임 후보를 생성한다. */
  private String createRandomNickname() {
    return "user_" + shortUuid();
  }

  /** 신규 OAuth 회원을 별도 트랜잭션에서 저장해 unique 충돌을 즉시 감지한다. */
  private Member saveOAuthMemberInNewTransaction(OAuthUserInfo userInfo, String nickname) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    return transactionTemplate.execute(
        status ->
            memberRepository.saveAndFlush(
                Member.createOAuthMember(
                    normalizeEmail(userInfo.email()),
                    nickname,
                    userInfo.provider(),
                    userInfo.providerId())));
  }

  /** 회원 생성 충돌 후 이미 생성된 회원이 있는지 다시 확인한다. */
  private Member resolveMemberAfterCreateConflict(OAuthUserInfo userInfo) {
    return memberRepository
        .findByOauthProviderAndOauthProviderId(userInfo.provider(), userInfo.providerId())
        .orElseGet(
            () ->
                memberRepository
                    .findByEmail(normalizeEmail(userInfo.email()))
                    .map(member -> connectOAuth(member, userInfo))
                    .orElse(null));
  }

  /** Google 이름을 닉네임 후보로 사용할 수 있게 다듬는다. */
  private String normalizeNickname(String name) {
    if (name == null || name.isBlank()) {
      return "user_" + shortUuid();
    }
    return truncateNickname(name.trim(), NICKNAME_MAX_LENGTH);
  }

  /** 닉네임 최대 길이를 넘지 않도록 자른다. */
  private String truncateNickname(String value, int maxLength) {
    if (value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  /** 기본 닉네임 생성을 위한 짧은 UUID 문자열을 만든다. */
  private String shortUuid() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }
}
