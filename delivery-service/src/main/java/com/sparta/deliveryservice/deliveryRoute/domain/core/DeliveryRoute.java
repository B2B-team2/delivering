package com.sparta.deliveryservice.deliveryRoute.domain.core;

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
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.UUID;

@Getter
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "p_delivery_routes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "route_id", nullable = false, updatable = false)
    private UUID routeId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Column(name = "from_hub_name")
    private String fromHubName;

    @Column(name = "to_hub_name")
    private String toHubName;

    @Column(name = "estimated_distance", precision = 8, scale = 2)
    private BigDecimal estimatedDistance;

    @Column(name = "estimated_duration")
    private Time estimatedDuration;

    @Column(name = "actual_distance", precision = 8, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "actual_duration")
    private Time actualDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryRouteStatus status = DeliveryRouteStatus.PENDING;

    @Builder
    public DeliveryRoute(UUID deliveryId, Integer sequence, UUID fromHubId, UUID toHubId,
                         String fromHubName, String toHubName, // 추가
                         BigDecimal estimatedDistance, Time estimatedDuration,
                         DeliveryRouteStatus status) {
        this.deliveryId = deliveryId;
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.fromHubName = fromHubName; // 추가
        this.toHubName = toHubName;     // 추가
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.status = (status != null) ? status : DeliveryRouteStatus.PENDING;
    }

    public void updateRoute(Integer sequence, UUID fromHubId, UUID toHubId,
                            BigDecimal estimatedDistance, Time estimatedDuration,
                            BigDecimal actualDistance, Time actualDuration) {
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
    }

    public void updateStatus(DeliveryRouteStatus status, BigDecimal actualDistance, Time actualDuration) {
        this.status = status;
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
    }
}