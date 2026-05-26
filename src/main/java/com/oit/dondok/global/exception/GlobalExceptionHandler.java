package com.oit.dondok.global.exception;

import static com.oit.dondok.global.exception.GlobalErrorCode.INVALID_INPUT;
import static com.oit.dondok.global.exception.GlobalErrorCode.METHOD_NOT_SUPPORTED;
import static com.oit.dondok.global.exception.GlobalErrorCode.SERVER_ERROR;

import com.oit.dondok.global.exception.response.ApiResponse;

import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  // 비즈니스 예외
  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ApiResponse> handleCustomException(CustomException exception) {
    ErrorCode errorCode = exception.getErrorCode();

    if (exception.isServerError()) {
      log.error(
          "[SERVER_ERROR] code={}, message={}",
          errorCode.getCode(),
          exception.getMessage(),
          exception);
    } else {
      log.warn("[BUSINESS_ERROR] code={}, message={}", errorCode.getCode(), exception.getMessage());
    }

    ApiResponse response = ApiResponse.error(exception);

    return ResponseEntity.status(response.status()).body(response);
  }

  // 예상치 못한 서버 예외
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse> handleException(Exception exception) {
    log.error("Unexpected error occurred", exception);

    ApiResponse response = ApiResponse.error(SERVER_ERROR);

    return ResponseEntity.status(response.status()).body(response);
  }

  // HTTP 요청 메서드 예외
  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ApiResponse response = ApiResponse.error(METHOD_NOT_SUPPORTED);

    return ResponseEntity.status(response.status()).body(response);
  }

  // 컨트롤러 메서드 파라미터 검증 예외
  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        exception.getAllErrors().stream()
            .map(MessageSourceResolvable::getDefaultMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));

    if (message.isBlank()) {
      message = INVALID_INPUT.getMessage();
    }

    ApiResponse response = ApiResponse.error(INVALID_INPUT, message);

    return ResponseEntity.status(response.status()).body(response);
  }

  // 요청 본문 DTO 검증 예외
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String message =
        exception.getBindingResult().getAllErrors().stream()
            .map(this::formatValidationError)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));

    if (message.isBlank()) {
      message = INVALID_INPUT.getMessage();
    }

    ApiResponse response = ApiResponse.error(INVALID_INPUT, message);

    return ResponseEntity.status(response.status()).body(response);
  }

  // 검증 오류 메시지 포맷팅
  private String formatValidationError(ObjectError error) {
    String defaultMessage = error.getDefaultMessage();

    if (defaultMessage == null) {
      return null;
    }

    if (error instanceof FieldError fieldError) {
      return fieldError.getField() + ": " + defaultMessage;
    }

    return defaultMessage;
  }
}
