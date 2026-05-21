package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "P002", "이미 완료된 결제입니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "P003", "이미 취소된 결제입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}