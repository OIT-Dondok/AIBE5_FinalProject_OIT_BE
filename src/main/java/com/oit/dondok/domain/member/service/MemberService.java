package com.oit.dondok.domain.member.service;

import com.oit.dondok.domain.member.dto.request.SignupRequest;
import com.oit.dondok.domain.member.dto.response.SignupResponse;
import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.domain.member.exception.MemberErrorCode;
import com.oit.dondok.domain.member.repository.MemberRepository;
import com.oit.dondok.global.exception.CustomException;
import com.oit.dondok.global.exception.GlobalErrorCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public SignupResponse signup(SignupRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    String nickname = request.nickname().trim();

    if (nickname.length() < 2 || nickname.length() > 50) {
      throw new CustomException(GlobalErrorCode.INVALID_INPUT);
    }

    if (memberRepository.existsByEmail(email)) {
      throw new CustomException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
    }

    if (memberRepository.existsByNickname(nickname)) {
      throw new CustomException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    String passwordHash = passwordEncoder.encode(request.password());

    Member member = Member.create(email, passwordHash, nickname);

    try {
      Member savedMember = memberRepository.saveAndFlush(member);
      return SignupResponse.from(savedMember);
    } catch (DataIntegrityViolationException exception) {
      String message = exception.getMostSpecificCause().getMessage();

      if (message != null && message.contains("uk_member_email")) {
        throw new CustomException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
      }

      if (message != null && message.contains("uk_member_nickname")) {
        throw new CustomException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
      }

      throw exception;
    }
  }
}
