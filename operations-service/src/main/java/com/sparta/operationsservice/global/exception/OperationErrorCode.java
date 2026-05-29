package com.sparta.operationsservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OperationErrorCode implements ErrorCode {

    CLAIM_NOT_FOUND(HttpStatus.NOT_FOUND, "CL001", "클레임 정보를 찾을 수 없습니다.", null),
    INVALID_CLAIM_STATUS(HttpStatus.BAD_REQUEST, "CL002", "유효하지 않은 클레임 상태입니다.", "status"),
    DUPLICATE_CLAIM(HttpStatus.CONFLICT, "CL003", "이미 해당 업체 주문에 대한 클레임이 존재합니다.", "companyOrderId"),

    SLACK_NOT_FOUND(HttpStatus.NOT_FOUND, "SL001", "클레임 정보를 찾을 수 없습니다.", "SlackMessageId"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "SL001", "인가되지 않은 접근입니다.", "User"),

    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "AI-001", "요청하신 AI 기록을 찾을 수 없습니다.", "AiHistory");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String field;

    @Override
    public String getField() {
        return this.field;
    }
}
