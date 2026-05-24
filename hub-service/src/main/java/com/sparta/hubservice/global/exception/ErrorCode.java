package com.sparta.hubservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements com.sparta.common.dto.ErrorCode {

    // Hub
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "허브를 찾을 수 없습니다."),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "H002", "허브 경로를 찾을 수 없습니다."),
    HUB_IN_USE(HttpStatus.CONFLICT, "H003", "HUB_IN_USE"),

    // Warehouse
    WAREHOUSE_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "물류 창고를 찾을 수 없습니다."),
    DUPLICATE_WAREHOUSE(HttpStatus.CONFLICT, "W002", "해당 허브에 이미 창고가 존재합니다."),

    // Inventory
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "재고를 찾을 수 없습니다."),
    DUPLICATE_INVENTORY(HttpStatus.CONFLICT, "I002", "해당 창고에 이미 동일한 상품 옵션의 재고가 존재합니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "I003", "재고가 부족합니다."),
    INVALID_STOCK_OPERATION(HttpStatus.BAD_REQUEST, "I004", "유효하지 않은 재고 조작입니다."),
    OPTIMISTIC_LOCK_FAILURE(HttpStatus.CONFLICT, "I005", "동시 요청입니다. 잠시 후 재시도해 주세요."),
    CANCEL_QUANTITY_EXCEEDED(HttpStatus.CONFLICT, "I006", "취소 수량이 예약된 수량을 초과합니다."),
    INVENTORY_HAS_STOCK(HttpStatus.CONFLICT, "I007", "잔여 수량 또는 예약 수량이 있는 재고는 삭제할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
