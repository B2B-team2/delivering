package com.sparta.orderservice.payment.domain.core;


import com.sparta.common.dto.BusinessException;
import com.sparta.common.entity.BaseEntity;
import com.sparta.orderservice.global.exception.PaymentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;                           // p_orders FK

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;   // 현재는 카드 결제만 허용

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;                      // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "pg_transaction_id", length = 255)
    private String pgTransactionId;                 // 거래 ID (실제 PG 연동 X, mock UUID 자동 생성)

    /**
     * 결제 요청 생성 (PENDING 상태)
     * 결제 금액은 이 시점에 확정되며, 거래 ID는 confirm 단계에서 자동 생성됨
     */
    public static Payment ready(UUID orderId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        return payment;
    }

    /**
     * 결제 승인 확정: PENDING → COMPLETED
     * 실제 PG 연동 X, mock UUID를 거래 ID로 자동 생성
     */
    public void confirm() {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_COMPLETED);
        }
        if (this.status == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        this.pgTransactionId = UUID.randomUUID().toString();
        this.status = PaymentStatus.COMPLETED;
    }

    /**
     * 결제 취소/환불: PENDING 또는 COMPLETED → CANCELLED
     */
    public void cancel(String deletedBy) {
        if (this.status == PaymentStatus.CANCELLED) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        this.status = PaymentStatus.CANCELLED;
        this.softDelete(deletedBy);
    }
}