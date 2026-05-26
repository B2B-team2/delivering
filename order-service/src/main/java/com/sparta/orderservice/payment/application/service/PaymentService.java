package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.core.PaymentStatus;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import com.sparta.orderservice.order.application.service.OrderService;
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
    private final OrderService orderService;

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
     * 상태 검증은 OrderService에 위임
     *
     * 흐름:
     * cancelPayment() → cancelOrder() → OrderCancelledEvent 발행
     *   → cancelPaymentByOrderId()에서 Payment CANCELLED 처리
     *
     * Order를 취소의 단일 진입점으로 사용:
     * - cancelPayment() 경유: 결제 API → 주문 취소 위임 → 이벤트 → 결제 취소
     * - cancelOrder() 직접 경유: 주문 취소 → 이벤트 → 결제 취소
     * 두 경로 모두 cancelPaymentByOrderId()에서만 결제 상태를 변경
     */
    @Transactional
    public PaymentResult cancelPayment(UUID paymentId, UUID requesterId) {
        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        if (!orderService.isCancellable(payment.getOrderId())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        // 주문 취소 위임 → OrderCancelledEvent 발행 → cancelPaymentByOrderId()에서 결제 취소
        // @EventListener 동기 실행(같은 TX)이므로 리턴 시점에 Payment는 이미 CANCELLED 상태
        orderService.cancelOrder(payment.getOrderId(), requesterId);

        return PaymentResult.from(payment);
    }

    /**
     * 주문 취소 이벤트 수신 시 결제 취소 (OrderCancelledEvent 핸들러에서 호출)
     * Payment 상태 변경의 단일 책임 지점 — cancelPayment() / cancelOrder() 양쪽 경로 모두 여기서 처리
     */
    @Transactional
    public void cancelPaymentByOrderId(UUID orderId, UUID requesterId) {
        paymentRepository.findPaymentByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.CANCELLED) return;
            payment.cancel(requesterId);
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
