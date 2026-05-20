package com.sparta.companyservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    INVALID_COMPANY_TYPE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 업체 타입입니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "업체를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
