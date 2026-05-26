package com.oit.dondok.global.exception.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oit.dondok.global.exception.CustomException;
import com.oit.dondok.global.exception.ErrorCode;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ApiResponse(@JsonIgnore HttpStatus status, String code, String message) {

  public static ApiResponse error(CustomException exception) {
    ErrorCode errorCode = exception.getErrorCode();

    return ApiResponse.builder()
        .status(errorCode.getStatus())
        .code(errorCode.getCode())
        .message(exception.getMessage())
        .build();
  }

  public static ApiResponse error(ErrorCode errorCode) {
    return ApiResponse.builder()
        .status(errorCode.getStatus())
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();
  }

  public static ApiResponse error(ErrorCode errorCode, String message) {
    return ApiResponse.builder()
        .status(errorCode.getStatus())
        .code(errorCode.getCode())
        .message(message)
        .build();
  }

  public static ApiResponse of(HttpStatus status, String code, String message) {
    return ApiResponse.builder().status(status).code(code).message(message).build();
  }
}
