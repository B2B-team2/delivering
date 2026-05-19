package com.sparta.orderservice.order.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id")
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_order_id", nullable = false)
    private CompanyOrder companyOrder;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "product_option_id", nullable = false)
    private UUID productOptionId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public static OrderItem of(CompanyOrder companyOrder, UUID productOptionId, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.companyOrder = companyOrder;
        item.productOptionId = productOptionId;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }

    public void assignDelivery(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
}
