package com.oit.dondok.infra.image.dto;

import lombok.Getter;

@Getter
public class PresignedUrlRequest {
  private Long crewId;
  private Long memberId;
}
