package com.sparta.deliveryservice.delivery.domain.core;

import com.sparta.common.entity.BaseEntity;
import com.sparta.deliveryservice.delivery.infrastructure.converter.DeliveryAddressConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "p_deliveries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(name = "company_order_id", nullable = false)
    private UUID companyOrderId;

    @Column(name = "company_receive_id", nullable = false)
    private UUID companyReceiveId;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "departure_hub_id", nullable = false)
    private UUID departureHubId;

    @Column(name = "destination_hub_id", nullable = false)
    private UUID destinationHubId;

    @Convert(converter = DeliveryAddressConverter.class)
    @Column(name = "delivery_address", nullable = false, length = 255)
    private DeliveryAddress deliveryAddress;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "postal_code", nullable = false, length = 5)
    private String postalCode;

    @Column(name = "recipient_slack_id", length = 100)
    private String recipientSlackId;

    @Column(name = "delivery_manager_id")
    private UUID deliveryManagerId;

    @Column(name = "final_dispatch_deadline_at")
    private LocalDateTime finalDispatchDeadlineAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Delivery(UUID companyOrderId, UUID companyReceiveId, String trackingNumber, DeliveryStatus status, String memo,
                    UUID departureHubId, UUID destinationHubId, DeliveryAddress  deliveryAddress,
                    String recipientName, String managerName, String managerPhone, String phone, String postalCode, String recipientSlackId, UUID deliveryManagerId,
                    LocalDateTime finalDispatchDeadlineAt) {
        this.companyOrderId = companyOrderId;
        this.companyReceiveId = companyReceiveId;
        this.trackingNumber = trackingNumber;
        this.status = (status != null) ? status : DeliveryStatus.PENDING;
        this.memo = memo;
        this.departureHubId = departureHubId;
        this.destinationHubId = destinationHubId;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.phone = phone;
        this.postalCode = postalCode;
        this.recipientSlackId = recipientSlackId;
        this.deliveryManagerId = deliveryManagerId;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.finalDispatchDeadlineAt = finalDispatchDeadlineAt;
    }

    public void updateDelivery(UUID deliveryManagerId, String memo, DeliveryAddress deliveryAddress,
                               String recipientName, String phone, String postalCode, String recipientSlackId, LocalDateTime finalDispatchDeadlineAt) {
        this.deliveryManagerId = deliveryManagerId;
        this.memo = memo;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.phone = phone;
        this.postalCode = postalCode;
        this.recipientSlackId = recipientSlackId;
        this.finalDispatchDeadlineAt = finalDispatchDeadlineAt;
    }

    public void updateStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void updateDeliveryManager(UUID deliveryManagerId, String managerName, String managerPhone) {
        this.deliveryManagerId = deliveryManagerId;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
    }

    public void startDelivery(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = DeliveryStatus.SHIPPED;
        this.startedAt = LocalDateTime.now();
    }

    public void completeDelivery(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = DeliveryStatus.DELIVERED;
        this.completedAt = LocalDateTime.now();
    }

    public void assignDeliveryManager(UUID deliveryManagerId, String managerName, String managerPhone) {
        this.deliveryManagerId = deliveryManagerId;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
    }
}