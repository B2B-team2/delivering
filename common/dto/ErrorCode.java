package com.sparta.delivhub.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}