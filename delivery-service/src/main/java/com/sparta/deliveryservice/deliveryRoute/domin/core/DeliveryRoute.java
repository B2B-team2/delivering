package com.sparta.deliveryservice.deliveryRoute.domin.core;

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
import java.time.LocalDateTime;
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

    // 소수점 8자리 중 소수점 아래 2자리 표현 (NUMERIC(8,2))
    @Column(name = "estimated_distance", precision = 8, scale = 2)
    private BigDecimal estimatedDistance;

    // 테이블 스펙 상 TIMESTAMP 타입으로 기록된 예상 소요 시간/시각
    @Column(name = "estimated_duration")
    private LocalDateTime estimatedDuration;

    @Column(name = "actual_distance", precision = 8, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "actual_duration")
    private LocalDateTime actualDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteStatus status = RouteStatus.PENDING;

    @Builder
    public DeliveryRoute(UUID deliveryId, Integer sequence, UUID fromHubId, UUID toHubId,
                         BigDecimal estimatedDistance, LocalDateTime estimatedDuration,
                         BigDecimal actualDistance, LocalDateTime actualDuration, RouteStatus status) {
        this.deliveryId = deliveryId;
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
        this.status = (status != null) ? status : RouteStatus.PENDING;
    }

    public void updateRoute(Integer sequence, UUID fromHubId, UUID toHubId,
                            BigDecimal estimatedDistance, LocalDateTime estimatedDuration,
                            BigDecimal actualDistance, LocalDateTime actualDuration) {
        this.sequence = sequence;
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
    }

    public void updateStatus(RouteStatus status, BigDecimal actualDistance, LocalDateTime actualDuration) {
        this.status = status;
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
    }
}