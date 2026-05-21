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

    // Warehouse
    WAREHOUSE_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "물류 창고를 찾을 수 없습니다."),
    DUPLICATE_WAREHOUSE(HttpStatus.CONFLICT, "W002", "해당 허브에 이미 창고가 존재합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
