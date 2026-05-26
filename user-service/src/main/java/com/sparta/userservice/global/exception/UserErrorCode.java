package com.sparta.userservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "U009", "유효하지 않은 Refresh Token 입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    CANNOT_REGISTER_AS_MASTER(HttpStatus.FORBIDDEN, "U003", "MASTER 권한으로 가입할 수 없습니다."),
    USER_NOT_APPROVED(HttpStatus.FORBIDDEN, "U004", "승인되지 않은 사용자입니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "U005", "이미 삭제된 사용자입니다."),
    INVALID_APPROVAL_STATUS(HttpStatus.BAD_REQUEST, "U006", "잘못된 승인 상태입니다."),
    DELIVERY_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "U007", "배송담당자를 찾을 수 없습니다."),
    NO_AVAILABLE_DELIVERY_MANAGER(HttpStatus.NOT_FOUND, "U008", "배정 가능한 배송담당자가 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "U011", "접근 권한이 없습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "U010", "유효하지 않은 역할입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}