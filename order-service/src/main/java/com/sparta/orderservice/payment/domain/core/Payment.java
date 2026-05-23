package com.sparta.orderservice.payment.domain.core;


import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(name = "pg_transaction_id", length = 255)
    private String pgTransactionId;                 // 거래 ID (실제 PG 연동 X, mock UUID 자동 생성)

    /**
     * 선결제 완료: 주문 생성과 동시에 COMPLETED 상태로 결제 확정
     * mock UUID를 pgTransactionId로 자동 생성
     */
    public static Payment complete(UUID orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.amount = amount;
        payment.pgTransactionId = UUID.randomUUID().toString();
        payment.status = PaymentStatus.COMPLETED;
        return payment;
    }

    /**
     * 결제 취소/환불: COMPLETED → CANCELLED
     */
    public void cancel(String cancelledBy) {
        this.status = PaymentStatus.CANCELLED;
        this.softDelete(cancelledBy);
    }
}