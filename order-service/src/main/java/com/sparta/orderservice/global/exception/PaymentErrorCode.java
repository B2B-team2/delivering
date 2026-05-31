package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "P002", "이미 취소된 결제입니다."),
    PAYMENT_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "P003", "출고가 시작된 주문의 결제는 취소할 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "P004", "해당 결제에 대한 접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}