package com.sparta.companyservice.global.exception;

import com.sparta.common.dto.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    INVALID_COMPANY_TYPE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 업체 타입입니다.", "type"),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "업체를 찾을 수 없습니다.", null),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "C003", "이미 등록된 사업자 번호입니다.", "businessNumber"),
    PRODUCT_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "P001", "상품 상태는 필수입니다.", "status");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String field;

    @Override
    public String getField() {
        return this.field;
    }
}
