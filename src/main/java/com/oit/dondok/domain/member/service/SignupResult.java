package com.oit.dondok.domain.member.service;

import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.domain.member.entity.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record SignupResult(
    UUID memberUuid, String email, String nickname, MemberStatus status, LocalDateTime createdAt) {

  static SignupResult from(Member member) {
    return new SignupResult(
        member.getUuid(),
        member.getEmail(),
        member.getNickname(),
        member.getStatus(),
        member.getCreatedAt());
  }
}
