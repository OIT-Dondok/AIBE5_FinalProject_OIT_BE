package com.oit.dondok.global.exception;

import static com.oit.dondok.global.exception.GlobalErrorCode.INVALID_INPUT;
import static com.oit.dondok.global.exception.GlobalErrorCode.METHOD_NOT_SUPPORTED;
import static com.oit.dondok.global.exception.GlobalErrorCode.NOT_FOUND;
import static com.oit.dondok.global.exception.GlobalErrorCode.SERVER_ERROR;
import static com.oit.dondok.global.exception.GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE;

import com.oit.dondok.global.exception.dto.response.ErrorResponse;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private ResponseEntity<Object> errorResponse(ErrorCode errorCode) {
    ErrorResponse response = ErrorResponse.error(errorCode);

    return ResponseEntity.status(response.status()).body(response);
  }

  private ResponseEntity<Object> errorResponse(ErrorCode errorCode, String message) {
    ErrorResponse response = ErrorResponse.error(errorCode, message);

    return ResponseEntity.status(response.status()).body(response);
  }

  private ResponseEntity<Object> errorResponse(ErrorCode errorCode, HttpHeaders headers) {
    ErrorResponse response = ErrorResponse.error(errorCode);

    return ResponseEntity.status(response.status()).headers(headers).body(response);
  }

  // 비즈니스 예외
  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
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

    ErrorResponse response = ErrorResponse.error(exception);

    return ResponseEntity.status(response.status()).body(response);
  }

  // 예상치 못한 서버 예외
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception exception) {
    log.error("Unexpected error occurred", exception);

    ErrorResponse response = ErrorResponse.error(SERVER_ERROR);

    return ResponseEntity.status(response.status()).body(response);
  }

  // HTTP 요청 메서드 예외
  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ErrorResponse response = ErrorResponse.error(METHOD_NOT_SUPPORTED);

    return ResponseEntity.status(response.status()).headers(headers).body(response);
  }

  // malformed JSON 예외
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return errorResponse(INVALID_INPUT);
  }

  // 필수 query parameter 누락
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return errorResponse(INVALID_INPUT);
  }

  // query/path 타입 변환 실패
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (exception instanceof MethodArgumentTypeMismatchException methodException) {
      String message = methodException.getName() + " 파라미터 타입이 올바르지 않습니다.";
      return errorResponse(INVALID_INPUT, message);
    }

    return errorResponse(INVALID_INPUT);
  }

  // Content-Type 오류
  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return errorResponse(UNSUPPORTED_MEDIA_TYPE, headers);
  }

  // 정적 리소스 404
  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return errorResponse(NOT_FOUND);
  }

  // 컨트롤러 메서드 파라미터 검증 예외
  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (exception.getStatusCode().is5xxServerError()) {
      return errorResponse(SERVER_ERROR);
    }

    String message =
        exception.getAllErrors().stream()
            .map(MessageSourceResolvable::getDefaultMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));

    if (message.isBlank()) {
      message = INVALID_INPUT.getMessage();
    }

    return errorResponse(INVALID_INPUT, message);
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

    ErrorResponse response = ErrorResponse.error(INVALID_INPUT, message);

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
