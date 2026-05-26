package com.sparta.common.handler;

import com.sparta.common.dto.ErrorResponse;
import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.CommonErrorCode;
import com.sparta.common.dto.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 만든 비즈니스 예외 처리 (throw new BusinessException(...))
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        String errorName = (errorCode instanceof Enum) ? ((Enum<?>) errorCode).name() : "BUSINESS_ERROR";

        List<ErrorResponse.FieldErrorDetail> fieldErrors = null;
        if (errorCode.getField() != null) {
            fieldErrors = List.of(ErrorResponse.FieldErrorDetail.builder()
                    .field(errorCode.getField())
                    .message(errorCode.getMessage())
                    .build());
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorName)
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    // 2. 입력값 검증 실패 시 (@Valid 에러)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("VALIDATION_ERROR")
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. 그 외 알 수 없는 모든 에러 방어
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Server Error: ", e);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message("SERVER_ERROR")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 4. JSON 매핑 실패 시 ( Enum 오타 등 ) 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("INVALID_JSON_FORMAT")
                .build();
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 5. 역할에 따른 접근 권한 제한 시
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = CommonErrorCode.ACCESS_DENIED;
        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    // 6. 경로 변수 타입 불일치 처리 (MethodArgumentTypeMismatchException)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Parameter Type Mismatch: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("INVALID_PARAMETER_TYPE")
                .errors(List.of(ErrorResponse.FieldErrorDetail.builder()
                        .field(e.getName())
                        .message(String.format("'%s'은(는) 유효한 %s 형식이 아닙니다.", e.getValue(), e.getRequiredType().getSimpleName()))
                        .build()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 7. 필수 경로 변수 누락 처리 (MissingPathVariableException)
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(MissingPathVariableException e) {
        log.warn("Missing Path Variable: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("MISSING_PATH_VARIABLE")
                .errors(List.of(ErrorResponse.FieldErrorDetail.builder()
                        .field(e.getVariableName())
                        .message("필수 경로 변수가 누락되었습니다.")
                        .build()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 8. 필수 쿼리 파라미터 누락 처리 (MissingServletRequestParameterException)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("MISSING_QUERY_PARAMETER")
                .errors(List.of(ErrorResponse.FieldErrorDetail.builder()
                        .field(e.getParameterName())
                        .message("필수 쿼리 파라미터가 누락되었습니다.")
                        .build()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 9. 지원하지 않는 HTTP 메서드 요청 처리 (HttpRequestMethodNotSupportedException)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Allowed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .message("METHOD_NOT_ALLOWED")
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
}
