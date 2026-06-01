package com.oit.dondok.infra.image.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PresignedUrlResponse(String uploadUrl, String s3Key) {

  public static PresignedUrlResponse of(String uploadUrl, String s3Key) {
    return new PresignedUrlResponse(uploadUrl, s3Key);
  }
}
