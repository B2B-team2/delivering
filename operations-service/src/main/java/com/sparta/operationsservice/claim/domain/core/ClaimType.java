package com.sparta.operationsservice.claim.domain.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimType {
    RETURN("반품"),
    EXCHANGE("교환");

    private final String description;
}
