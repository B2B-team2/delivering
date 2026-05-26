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
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "상품을 찾을 수 없습니다.", null),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "P003", "유효하지 않은 상품 상태입니다.", "status"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "카테고리를 찾을 수 없습니다.", null),
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "상품 옵션을 찾을 수 없습니다.", null),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "P005", "이미 존재하는 카테고리 이름입니다.", "name");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String field;

    @Override
    public String getField() {
        return this.field;
    }
}
