package com.oit.dondok.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.domain.member.entity.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record SignupResponse(
    @JsonProperty("member_uuid") UUID memberUuid,
    String email,
    String nickname,
    @JsonProperty("profile_image_s3_key") String profileImageS3Key,
    MemberStatus status,
    @JsonProperty("created_at") LocalDateTime createdAt) {
  public static SignupResponse from(Member member) {
    return new SignupResponse(
        member.getUuid(),
        member.getEmail(),
        member.getNickname(),
        member.getProfileImageS3Key(),
        member.getStatus(),
        member.getCreatedAt());
  }
}
