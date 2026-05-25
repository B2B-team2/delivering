 package com.sparta.deliveryservice.deliveryLog.domin.core;

 import com.sparta.common.entity.BaseEntity;
 import jakarta.persistence.Column;
 import jakarta.persistence.Entity;
 import jakarta.persistence.EntityListeners;
 import jakarta.persistence.GeneratedValue;
 import jakarta.persistence.GenerationType;
 import jakarta.persistence.Id;
 import jakarta.persistence.Table;
 import lombok.AccessLevel;
 import lombok.Builder;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import org.hibernate.annotations.JdbcTypeCode;
 import org.hibernate.annotations.SQLRestriction;
 import org.hibernate.type.SqlTypes;
 import org.springframework.data.jpa.domain.support.AuditingEntityListener;

 import java.util.UUID;

@Getter
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "p_delivery_log")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryLog extends BaseEntity {

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_value", columnDefinition = "json")
    private String previousValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_value", columnDefinition = "json")
    private String currentValue;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

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