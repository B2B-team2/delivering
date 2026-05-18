package com.sparta.deliveryservice.deliveries.entity;

import com.sparta.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_deliveries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(name = "company_order_id", nullable = false)
    private UUID companyOrderId;

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

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_slack_id", length = 100)
    private String recipientSlackId;

    @Column(name = "delivery_manager_id", nullable = false)
    private UUID deliveryManagerId;

    @Column(name = "final_dispatch_deadline_at")
    private LocalDateTime finalDispatchDeadlineAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Delivery(UUID companyOrderId, String trackingNumber, DeliveryStatus status, String memo,
                    UUID departureHubId, UUID destinationHubId, String deliveryAddress,
                    String recipientName, String recipientSlackId, UUID deliveryManagerId,
                    LocalDateTime finalDispatchDeadlineAt) {
        this.companyOrderId = companyOrderId;
        this.trackingNumber = trackingNumber;
        this.status = (status != null) ? status : DeliveryStatus.PENDING;
        this.memo = memo;
        this.departureHubId = departureHubId;
        this.destinationHubId = destinationHubId;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientSlackId = recipientSlackId;
        this.deliveryManagerId = deliveryManagerId;
        this.finalDispatchDeadlineAt = finalDispatchDeadlineAt;
    }

    public void startDelivery(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = DeliveryStatus.SHIPPED;
        this.startedAt = LocalDateTime.now();
    }

    public void completeDelivery() {
        this.status = DeliveryStatus.DELIVERED;
        this.completedAt = LocalDateTime.now();
    }
}