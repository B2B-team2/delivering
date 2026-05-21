package com.sparta.companyservice.products.domain.core;

import lombok.Getter;

@Getter
public enum ProductStatusEnum {
    ON_SALE("판매 중"),
    SOLD_OUT("품절"),
    DISCONTINUED("판매 중단");

    private final String description;

    ProductStatusEnum(String description) {
        this.description = description;
    }
}
