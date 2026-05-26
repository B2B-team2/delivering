package com.sparta.hubservice.inventory.domain.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_inventory_histories", schema = "hub-db")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @Column(nullable = false)
    private UUID inventoryId;

    @Column
    private UUID orderId;

    @Column
    private UUID companyOrderId;

    @Column(nullable = false)
    private int changeQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryChangeType changeType;

    @Column(length = 255)
    private String reason;

    @Builder
    public InventoryHistory(UUID inventoryId, UUID orderId, UUID companyOrderId, int changeQuantity, InventoryChangeType changeType, String reason) {
        this.inventoryId = inventoryId;
        this.orderId = orderId;
        this.companyOrderId = companyOrderId;
        this.changeQuantity = changeQuantity;
        this.changeType = changeType;
        this.reason = reason;
    }
}
