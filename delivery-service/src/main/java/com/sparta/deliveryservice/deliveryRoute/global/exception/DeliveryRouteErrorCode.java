package com.sparta.deliveryservice.deliveryRoute.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryRouteErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_ROUTE_STATUS(HttpStatus.BAD_REQUEST, "DR001", "유효하지 않은 경로 상태입니다."),

    // 404 Not Found
    DELIVERY_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "DR002", "배송 경로를 찾을 수 없습니다."),

    // 403 Forbidden
    ROUTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DR003", "해당 경로에 대한 수정 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}