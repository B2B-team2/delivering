package com.sparta.hubservice.hubroute.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_hub_routes", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "route_id")
    private UUID routeId;

    @Column(nullable = false)
    private UUID fromHubId;

    @Column(nullable = false)
    private UUID toHubId;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private BigDecimal distance;

    @Builder
    public HubRoute(UUID fromHubId, UUID toHubId, int duration, BigDecimal distance) {
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.duration = duration;
        this.distance = distance;
    }
}
