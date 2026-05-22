package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DraftErrorCode implements ErrorCode {

    DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "임시주문 항목을 찾을 수 없습니다."),
    DRAFT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "D002", "해당 임시주문 항목에 대한 접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
