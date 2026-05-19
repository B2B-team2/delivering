package com.sparta.companyservice.company.domain.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompanyTypeEnum {
    PRODUCER("생산업체"),
    RECEIVER("수령업체");

    private final String description;
}
