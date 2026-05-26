package com.sparta.userservice.user.domain.entity;

import com.sparta.common.entity.BaseEntity;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import com.sparta.userservice.user.domain.enums.ManagerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_delivery_managers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManager extends BaseEntity {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_type", nullable = false, length = 100)
    private ManagerType managerType;

    @Column(name = "delivery_order", nullable = false)
    private Integer deliveryOrder;

    @Column(name = "hub_id")
    private UUID hubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private DeliveryManagerStatus status;

    @Column(name = "last_assigned_at")
    private LocalDateTime lastAssignedAt;

    public static DeliveryManager create(User user, ManagerType managerType, Integer deliveryOrder) {
        DeliveryManager manager = new DeliveryManager();
        manager.user = user;
        manager.managerType = managerType;
        manager.deliveryOrder = deliveryOrder;
        manager.status = DeliveryManagerStatus.WAITING;
        return manager;
    }

    public void assignHub(UUID hubId) {
        this.hubId = hubId;
    }

    public void updateLastAssigned() {
        this.lastAssignedAt = LocalDateTime.now();
    }

    public void updateStatus(DeliveryManagerStatus status) {
        this.status = status;
    }
}