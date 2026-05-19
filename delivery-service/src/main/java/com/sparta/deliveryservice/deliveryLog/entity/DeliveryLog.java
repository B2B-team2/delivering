 package com.sparta.deliveryservice.deliveryLog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "p_delivery_log")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    // 💡 PostgreSQL/MySQL 등의 JSON 타입을 자바의 String 형태로 안전하게 매핑
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_value", columnDefinition = "json")
    private String previousValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_value", columnDefinition = "json")
    private String currentValue;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreatedDate // 🌟 데이터 생성 시 현재 시간이 자동으로 기록됩니다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy // 🌟 데이터를 생성한 유저의 식별자(ID)가 자동으로 기록됩니다.
    @Column(name = "created_by", length = 36, updatable = false)
    private String createdBy;

    @Builder
    public DeliveryLog(UUID deliveryId, UUID routeId, String eventType, 
                       String previousValue, String currentValue, String reason) {
        this.deliveryId = deliveryId;
        this.routeId = routeId;
        this.eventType = eventType;
        this.previousValue = previousValue;
        this.currentValue = currentValue;
        this.reason = reason;
    }

}