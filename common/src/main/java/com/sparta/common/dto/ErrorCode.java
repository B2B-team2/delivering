package com.sparta.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "해당 작업을 수행할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}