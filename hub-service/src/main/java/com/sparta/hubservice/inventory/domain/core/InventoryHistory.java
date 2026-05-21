package com.sparta.hubservice.inventory.domain.core;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_inventory_histories", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @Column(nullable = false)
    private UUID inventoryId;

    @Column
    private UUID orderId;

    @Column(nullable = false)
    private int changeQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryChangeType changeType;

    @Builder
    public InventoryHistory(UUID inventoryId, UUID orderId, int changeQuantity, InventoryChangeType changeType) {
        this.inventoryId = inventoryId;
        this.orderId = orderId;
        this.changeQuantity = changeQuantity;
        this.changeType = changeType;
    }
}
