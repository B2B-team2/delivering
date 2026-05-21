package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import com.sparta.orderservice.payment.application.dto.CreatePaymentCommand;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 생성: PENDING 상태로 저장
     * 실제 결제 승인은 PATCH /payments/{paymentId}/confirm 에서 처리
     */
    @Transactional
    public PaymentResult createPayment(CreatePaymentCommand command) {
        PaymentMethod paymentMethod = PaymentMethod.valueOf(command.paymentMethod());
        Payment payment = Payment.ready(command.orderId(), paymentMethod, command.amount());
        paymentRepository.save(payment);
        return PaymentResult.from(payment);
    }

    /**
     * 결제 확정: PENDING → COMPLETED
     * 실제 PG 연동 X — mock UUID를 pgTransactionId로 자동 생성
     * 이미 완료/취소된 결제에 confirm 시도 시 예외 발생 (Payment.confirm 내부 검증)
     */
    @Transactional
    public PaymentResult confirmPayment(UUID paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        payment.confirm();
        return PaymentResult.from(payment);
    }

    /**
     * 결제 취소: PENDING → CANCELLED
     * 이미 취소된 결제에 재취소 시도 시 예외 발생 (Payment.cancel 내부 검증)
     */
    @Transactional
    public PaymentResult cancelPayment(UUID paymentId, UUID requesterId) {
        Payment payment = findPaymentOrThrow(paymentId);
        payment.cancel(requesterId.toString());
        return PaymentResult.from(payment);
    }

    /**
     * 결제 단건 조회
     */
    public PaymentResult getPayment(UUID paymentId) {
        return PaymentResult.from(findPaymentOrThrow(paymentId));
    }

    /**
     * 결제 목록 조회
     */
    public Page<PaymentResult> getPayments(Pageable pageable) {
        // TODO: 권한별 필터링 (마스터/허브관리자 → 전체, 업체 담당자 → 자기 회사 결제만)
        return paymentRepository.findAllPayments(pageable)
                .map(PaymentResult::from);
    }

    private Payment findPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findPaymentById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

}
