package com.oit.dondok.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oit.dondok.domain.member.entity.MemberStatus;
import com.oit.dondok.domain.member.service.SignupResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record SignupResponse(
    @JsonProperty("member_uuid") UUID memberUuid,
    String email,
    String nickname,
    MemberStatus status,
    @JsonProperty("created_at") LocalDateTime createdAt) {

  public static SignupResponse from(SignupResult result) {
    return new SignupResponse(
        result.memberUuid(),
        result.email(),
        result.nickname(),
        result.status(),
        result.createdAt());
  }
}
