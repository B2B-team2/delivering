package com.sparta.hubservice.warehouse.domain.core;

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
@Table(name = "p_warehouses", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(nullable = false, unique = true)
    private UUID hubId;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column
    private String address;

    @Column
    private String region;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    private WarehouseStatus status;

    @Builder
    public Warehouse(UUID hubId, String warehouseName, String address, String region,
                     String contactPhone, WarehouseStatus status) {
        this.hubId = hubId;
        this.warehouseName = warehouseName;
        this.address = address;
        this.region = region;
        this.contactPhone = contactPhone;
        this.status = status;
    }

    public void update(String warehouseName, String address, String region,
                       String contactPhone, WarehouseStatus status) {
        if (warehouseName != null) this.warehouseName = warehouseName;
        if (address != null) this.address = address;
        if (region != null) this.region = region;
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (status != null) this.status = status;
    }
}
