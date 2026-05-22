package com.sparta.orderservice.order.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_company_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_order_id")
    private UUID companyOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "subtotal_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalPrice;

    @Column(name = "subtotal_delivery_fee", precision = 8, scale = 2)
    private BigDecimal subtotalDeliveryFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CompanyOrderStatus status = CompanyOrderStatus.ORDERED;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;                            // 낙관적 락 — 동시 상태 전환 충돌 감지

    @OneToMany(mappedBy = "companyOrder", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static CompanyOrder of(Order order, UUID companyId, BigDecimal subtotalPrice, BigDecimal subtotalDeliveryFee) {
        CompanyOrder companyOrder = new CompanyOrder();
        companyOrder.order = order;
        companyOrder.companyId = companyId;
        companyOrder.subtotalPrice = subtotalPrice;
        companyOrder.subtotalDeliveryFee = subtotalDeliveryFee != null ? subtotalDeliveryFee : BigDecimal.ZERO;
        return companyOrder;
    }

    public void prepare() {
        this.status = CompanyOrderStatus.PREPARING;
    }

    public void ship() {
        this.status = CompanyOrderStatus.SHIPPED;
    }

    public void deliver() {
        this.status = CompanyOrderStatus.DELIVERED;
    }

    public void cancel(String deletedBy) {
        this.status = CompanyOrderStatus.CANCELLED;
        this.softDelete(deletedBy);
    }
}
