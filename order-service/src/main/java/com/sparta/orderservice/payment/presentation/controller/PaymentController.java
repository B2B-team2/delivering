package com.sparta.orderservice.payment.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.payment.application.service.PaymentService;
import com.sparta.orderservice.payment.presentation.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 취소 (COMPLETED → CANCELLED)
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") UUID requesterId
    ) {
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(paymentService.cancelPayment(paymentId, requesterId))));
    }

    // 결제 목록 조회
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PaymentResponse> responses = paymentService.getPayments(pageable)
                .map(PaymentResponse::from);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 결제 단건 조회
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(paymentService.getPayment(paymentId))));
    }
}
