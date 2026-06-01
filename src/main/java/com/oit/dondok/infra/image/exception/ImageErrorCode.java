package com.oit.dondok.infra.image.exception;

import com.oit.dondok.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {
  IMAGE_READ_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "이미지를 읽을 수 없습니다."),
  IMAGE_ENCODE_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "이미지 재인코딩에 실패했습니다."),
  IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;

  @Override
  public String getCode() {
    return name();
  }
}
