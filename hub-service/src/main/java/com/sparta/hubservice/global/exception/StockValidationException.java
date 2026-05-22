package com.sparta.hubservice.global.exception;

import com.sparta.common.dto.ErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class StockValidationException extends RuntimeException {

    private final List<ErrorResponse.FieldErrorDetail> errors;

    public StockValidationException(String field, String message) {
        super(message);
        this.errors = List.of(
                ErrorResponse.FieldErrorDetail.builder()
                        .field(field)
                        .message(message)
                        .build()
        );
    }
}
