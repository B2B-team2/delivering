package com.sparta.operationsservice.claim.domain.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimStatus {
    REQUESTED("접수"),
    PROCESSING("처리중"),
    REJECTED("반려"),
    COMPLETED("처리완료"),
    CANCELLED("취소");

    private final String description;
}
