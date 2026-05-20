package com.sparta.orderservice.order.domain.core;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    COMPANY_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "업체 주문을 찾을 수 없습니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O003", "이미 취소된 주문입니다."),
    COMPANY_ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O004", "이미 취소된 업체 주문입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
