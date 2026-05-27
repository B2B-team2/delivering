package com.sparta.hubservice.inventory.domain.core;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_warehouse_inventory", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarehouseInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private UUID productOptionId;

    @Column
    private UUID companyId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Column(nullable = false)
    private int safetyStock;

    @Version
    private Long version;

    @Builder
    public WarehouseInventory(UUID warehouseId, UUID productOptionId, UUID companyId, int quantity, int safetyStock) {
        this.warehouseId = warehouseId;
        this.productOptionId = productOptionId;
        this.companyId = companyId;
        this.quantity = quantity;
        this.reservedQuantity = 0;
        this.safetyStock = safetyStock;
    }

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void reserve(int qty) {
        if (getAvailableQuantity() < qty) {
            throw new BusinessException(com.sparta.hubservice.global.exception.ErrorCode.INSUFFICIENT_STOCK);
        }
        this.reservedQuantity += qty;
    }

    public void cancelReservation(int qty) {
        if (this.reservedQuantity < qty) {
            throw new BusinessException(com.sparta.hubservice.global.exception.ErrorCode.CANCEL_QUANTITY_EXCEEDED);
        }
        this.reservedQuantity -= qty;
    }

    public void deduct(int qty) {
        if (this.quantity < qty) {
            throw new BusinessException(com.sparta.hubservice.global.exception.ErrorCode.INSUFFICIENT_STOCK);
        }
        this.quantity -= qty;
        this.reservedQuantity -= qty;
    }

    public void adjust(int qty) {
        this.quantity += qty;
    }

    public void revertDeduct(int qty) {
        this.quantity += qty;
        this.reservedQuantity += qty;
    }

    public void returnStock(int qty) {
        this.quantity += qty;
    }

    public void updateSafetyStock(int safetyStock) {
        this.safetyStock = safetyStock;
    }
}
