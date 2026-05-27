package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import com.sparta.orderservice.order.application.service.OrderQueryService;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.core.PaymentStatus;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final AuthContext authContext;

    /**
     * 선결제: 주문 생성과 동시에 COMPLETED 상태로 결제 확정
     * OrderCommandService.createOrder() 내에서 같은 트랜잭션으로 호출됨
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
     * 상태 검증은 OrderQueryService에 위임
     *
     * 흐름:
     * cancelPayment() → cancelOrder() → OrderCancelledEvent 발행
     *   → cancelPaymentByOrderId()에서 Payment CANCELLED 처리
     *
     * Order를 취소의 단일 진입점으로 사용:
     * - cancelPayment() 경유: 결제 API → 주문 취소 위임 → 이벤트 → 결제 취소
     * - cancelOrder() 직접 경유: 주문 취소 → 이벤트 → 결제 취소
     * 두 경로 모두 cancelPaymentByOrderId()에서만 결제 상태를 변경
     *
     * NOT_SUPPORTED: 외부 TX 없이 실행 — cancelOrder가 자신의 독립 TX를 시작함
     * (기존 @Transactional이면 cancelOrder의 hub Feign 호출 중 DB 커넥션을 점유하는 문제 발생)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResult cancelPayment(UUID paymentId, UUID requesterId) {
        // MASTER, COMPANY_MANAGER만 결제 취소 가능
        if (!authContext.isMaster() && !authContext.isCompanyManager()) {
            throw new BusinessException(PaymentErrorCode.FORBIDDEN);
        }

        Payment payment = findPaymentOrThrow(paymentId);

        // COMPANY_MANAGER는 자기 회사가 수령업체인 주문의 결제만 취소 가능
        if (authContext.isCompanyManager()) {
            UUID receiverCompanyId = orderQueryService.getReceiverCompanyId(payment.getOrderId());
            if (!receiverCompanyId.equals(authContext.getCompanyId())) {
                throw new BusinessException(PaymentErrorCode.FORBIDDEN);
            }
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        if (!orderQueryService.isCancellable(payment.getOrderId())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        // cancelOrder가 자신의 독립 TX로 실행됨 (cancelPayment에 합류하지 않음)
        // → cancelPaymentByOrderId까지 포함한 모든 DB 변경이 cancelOrder TX 안에서 커밋됨
        UUID orderId = payment.getOrderId();
        orderCommandService.cancelOrder(orderId, requesterId);

        // payment는 TX 없이 로드된 detached 상태이므로 cancelPaymentByOrderId의 변경이 반영되지 않음
        // → 재조회하여 최신 CANCELLED 상태를 반환
        return PaymentResult.from(findPaymentOrThrow(paymentId));
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

    public PaymentResult getPayment(UUID paymentId) {
        if (!authContext.isMaster() && !authContext.isCompanyManager()) {
            throw new BusinessException(PaymentErrorCode.FORBIDDEN);
        }
        Payment payment = findPaymentOrThrow(paymentId);
        // COMPANY_MANAGER는 자기 회사가 수령업체인 주문의 결제만 조회 가능
        if (authContext.isCompanyManager()) {
            UUID receiverCompanyId = orderQueryService.getReceiverCompanyId(payment.getOrderId());
            if (!receiverCompanyId.equals(authContext.getCompanyId())) {
                throw new BusinessException(PaymentErrorCode.FORBIDDEN);
            }
        }
        return PaymentResult.from(payment);
    }

    public Page<PaymentResult> getPayments(Pageable pageable) {
        if (authContext.isMaster()) {
            return paymentRepository.findAllPayments(pageable).map(PaymentResult::from);
        }
        if (authContext.isCompanyManager()) {
            List<UUID> orderIds = orderQueryService.getOrderIdsByReceiverCompanyId(authContext.getCompanyId());
            if (orderIds.isEmpty()) return Page.empty(pageable);
            return paymentRepository.findPaymentsByOrderIds(orderIds, pageable).map(PaymentResult::from);
        }
        throw new BusinessException(PaymentErrorCode.FORBIDDEN);
    }

    private Payment findPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findPaymentById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

}
