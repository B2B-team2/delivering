package com.sparta.userservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    KEYCLOAK_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "A001", "이미 Keycloak에 등록된 이메일입니다."),
    KEYCLOAK_USER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "Keycloak 유저 생성에 실패했습니다."),
    KEYCLOAK_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A003", "Keycloak에서 유저를 찾을 수 없습니다."),
    KEYCLOAK_USER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A004", "Keycloak 유저 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
