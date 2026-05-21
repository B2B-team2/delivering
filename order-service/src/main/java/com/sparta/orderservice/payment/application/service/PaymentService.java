package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import com.sparta.orderservice.payment.domain.core.PaymentStatus;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
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
    private final OrderRepository orderRepository;

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
     * 취소 가능 조건:
     *   1) Order.status == PENDING (출고 전 — DELIVERING/COMPLETED/CANCELLED 이면 불가)
     *   2) 모든 CompanyOrder가 ORDERED 또는 PREPARING 상태
     *      - SHIPPED(배송중) 또는 DELIVERED(수령완료) 이면 취소 불가
     */
    @Transactional
    public PaymentResult cancelPayment(UUID paymentId, UUID requesterId) {
        Payment payment = findPaymentOrThrow(paymentId);

        Order order = orderRepository.findOrderById(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        boolean anyShipped = order.getCompanyOrders().stream()
                .anyMatch(co -> co.getStatus() == CompanyOrderStatus.SHIPPED
                        || co.getStatus() == CompanyOrderStatus.DELIVERED);
        if (anyShipped) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

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
