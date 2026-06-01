package com.oit.dondok.infra.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {
  private String presignedUrl;
  private String objectKey;
}
