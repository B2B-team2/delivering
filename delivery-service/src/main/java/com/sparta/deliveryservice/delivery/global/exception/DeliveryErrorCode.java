package com.sparta.deliveryservice.delivery.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "D001", "현재 상태에서 해당 처리를 할 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "D002", "잘못된 요청 형식입니다."),

    // 404 Not Found
    COMPANY_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "D003", "업체 주문을 찾을 수 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "D004", "배송 정보를 찾을 수 없습니다."),
    DELIVERY_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "D005", "배송 경로를 찾을 수 없습니다."),

    // 403 Forbidden (권한 관련)
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "D006", "해당 작업에 대한 권한이 없습니다."),

    // 409 Conflict
    DUPLICATE_DELIVERY(HttpStatus.CONFLICT, "D007", "이미 존재하는 배송 정보입니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "D008", "현재 처리 중인 주문입니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}