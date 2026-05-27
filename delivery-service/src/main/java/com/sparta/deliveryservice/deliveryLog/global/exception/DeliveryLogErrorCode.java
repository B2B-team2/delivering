package com.sparta.deliveryservice.deliveryLog.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryLogErrorCode implements ErrorCode {

    DELIVERY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DL001", "배송 로그를 찾을 수 없습니다."),

    // 400 Bad Request
    LOG_PARSING_ERROR(HttpStatus.BAD_REQUEST, "DL002", "로그 데이터 파싱 중 오류가 발생했습니다."),

    // 403 Forbidden
    LOG_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DL003", "해당 배송 로그에 대한 접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}