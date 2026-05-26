package com.sparta.hubservice.global.exception;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode code = (ErrorCode) e.getErrorCode();

        if (code == ErrorCode.HUB_IN_USE) {
            ErrorResponse response = ErrorResponse.builder()
                    .status(code.getHttpStatus().value())
                    .message(code.getMessage())
                    .errors(List.of(ErrorResponse.FieldErrorDetail.builder()
                            .field("hubId")
                            .message("해당 허브에 소속된 창고/업체/배송담당자가 존재하여 삭제할 수 없습니다.")
                            .build()))
                    .build();
            return ResponseEntity.status(code.getHttpStatus()).body(response);
        }

        if (code == ErrorCode.INVENTORY_HAS_STOCK) {
            ErrorResponse response = ErrorResponse.builder()
                    .status(code.getHttpStatus().value())
                    .message(code.getMessage())
                    .errors(List.of(ErrorResponse.FieldErrorDetail.builder()
                            .field("inventoryId")
                            .message(code.getMessage())
                            .build()))
                    .build();
            return ResponseEntity.status(code.getHttpStatus()).body(response);
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(code.getHttpStatus().value())
                .message(code.getMessage())
                .build();
        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponse.FieldErrorDetail.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("입력값이 올바르지 않습니다.")
                .errors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(StockValidationException.class)
    public ResponseEntity<ErrorResponse> handleStockValidation(StockValidationException e) {
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message(ErrorCode.INVALID_STOCK_OPERATION.getMessage())
                .errors(e.getErrors())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());
        ErrorCode code = ErrorCode.DUPLICATE_INVENTORY;
        ErrorResponse response = ErrorResponse.builder()
                .status(code.getHttpStatus().value())
                .message(code.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        log.warn("Optimistic lock failure: {}", e.getMessage());
        ErrorCode code = ErrorCode.OPTIMISTIC_LOCK_FAILURE;
        ErrorResponse response = ErrorResponse.builder()
                .status(code.getHttpStatus().value())
                .message(code.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type Mismatch: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("잘못된 형식의 파라미터입니다.")
                .build();
        return ResponseEntity.badRequest().body(response);
    }
}
