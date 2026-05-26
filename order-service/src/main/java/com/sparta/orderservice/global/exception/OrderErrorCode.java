package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다.", "orderId"),
    COMPANY_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "업체 주문을 찾을 수 없습니다.", "companyOrderId"),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O003", "이미 취소된 주문입니다.", "status"),
    COMPANY_ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O004", "이미 취소된 주문입니다.", "status"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "O005", "현재 상태에서 해당 처리를 할 수 없습니다.", "status"),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "O006", "요청하신 업체를 찾을 수 없습니다.", "companyId"),


    // 외부 서비스 연동 오류
    STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "O007", "재고가 부족합니다.", null),
    HUB_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "O008", "재고 서비스에 연결할 수 없습니다.", null),
    COMPANY_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "O009", "업체 서비스에 연결할 수 없습니다.", null),
    DELIVERY_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "O010", "배송 서비스에 연결할 수 없습니다.", null),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "O011", "외부 서비스 오류가 발생했습니다.", null),
    HUB_MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "O012", "해당 업체의 허브 매핑 정보를 찾을 수 없습니다.", "companyId");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String field;
}
