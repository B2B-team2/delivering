package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.core.PaymentStatus;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import com.sparta.orderservice.payment.application.port.OrderCancelPort;
import com.sparta.orderservice.payment.application.port.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderQueryPort orderQueryPort;
    private final OrderCancelPort orderCancelPort;

    /**
     * 선결제: 주문 생성과 동시에 COMPLETED 상태로 결제 확정
     * OrderService.createOrder() 내에서 같은 트랜잭션으로 호출됨
     */
    @Transactional
    public PaymentResult createCompletedPayment(UUID orderId, BigDecimal amount) {
        Payment payment = Payment.complete(orderId, PaymentMethod.CARD, amount);
        paymentRepository.save(payment);
        return PaymentResult.from(payment);
    }

    /**
     * 결제 취소/환불: COMPLETED → CANCELLED
     * 취소 가능 조건: Order.PENDING + CompanyOrder SHIPPED/DELIVERED 없을 때
     * 상태 검증은 OrderQueryPort(Adapter)에 위임
     *
     * 흐름:
     * 1. 결제 먼저 취소 (payment.cancel)
     * 2. OrderCancelPort로 주문 취소 위임 → OrderCancelledEvent 발행
     * 3. PaymentEventHandler.handleOrderCancelled → cancelPaymentByOrderId → 이미 취소됨(no-op)
     */
    @Transactional
    public PaymentResult cancelPayment(UUID paymentId, UUID requesterId) {
        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        if (!orderQueryPort.isCancellable(payment.getOrderId())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        // 결제 취소 먼저 처리 (이후 OrderCancelledEvent 수신 시 이미 CANCELLED → no-op)
        payment.cancel(requesterId.toString());

        // 주문 취소 위임 → 같은 트랜잭션에서 OrderCancelledEvent 발행 → 결제 취소 중복 방지 (idempotent)
        orderCancelPort.cancelOrder(payment.getOrderId(), requesterId);

        return PaymentResult.from(payment);
    }

    /**
     * 주문 취소 이벤트 수신 시 결제 취소 (OrderCancelledEvent 핸들러에서 호출)
     * 이미 취소된 결제는 건너뜀 (idempotent) — Payment cancel API 경유 시 중복 처리 방지
     */
    @Transactional
    public void cancelPaymentByOrderId(UUID orderId, UUID requesterId) {
        paymentRepository.findPaymentByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.CANCELLED) return; // 이미 취소됨 → no-op
            payment.cancel(requesterId.toString());
        });
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
