package com.sparta.orderservice.order.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "receiver_company_id", nullable = false)
    private UUID receiverCompanyId;         // 수령업체(주문자 COMPANY_MANAGER의 소속 업체)

    // 수령인 정보 스냅샷
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;           // 수령인 실명

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;                   // 수령인 연락처

    @Column(name = "slack_id", length = 36)
    private String slackId;                 // 수령인 Slack ID (nullable)

    // 배송 주소 JSON 스냅샷
    @Column(name = "address", nullable = false, columnDefinition = "jsonb")
    private String address;               // {"address": "기본주소", "address_detail": "상세주소"}

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;          // 납품 기한

    @Column(name = "request_memo", columnDefinition = "TEXT")
    private String requestMemo;             // 요청 사항 (nullable)

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;          // 상품 합계 금액

    @Column(name = "delivery_fee", precision = 8, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;   // 총 배송비

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;          // 최종 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<CompanyOrder> companyOrders = new ArrayList<>();

    public static Order of(
            UUID receiverCompanyId,
            String recipientName,
            String phone,
            String slackId,
            String address,
            LocalDateTime dueDate,
            String requestMemo,
            BigDecimal totalPrice,
            BigDecimal deliveryFee,
            BigDecimal finalPrice
    ) {
        Order order = new Order();
        order.receiverCompanyId = receiverCompanyId;
        order.recipientName = recipientName;
        order.phone = phone;
        order.slackId = slackId;
        order.address = address;
        order.dueDate = dueDate;
        order.requestMemo = requestMemo;
        order.totalPrice = totalPrice;
        order.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        order.finalPrice = finalPrice;
        return order;
    }

    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel(String deletedBy) {
        this.status = OrderStatus.CANCELLED;
        this.softDelete(deletedBy);
    }
}
