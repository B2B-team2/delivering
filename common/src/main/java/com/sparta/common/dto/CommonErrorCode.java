package com.sparta.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C001", "해당 작업을 수행할 권한이 없습니다."),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "C002", "잘못된 파라미터 타입입니다."),
    MISSING_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "C003", "필수 경로 변수가 누락되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
