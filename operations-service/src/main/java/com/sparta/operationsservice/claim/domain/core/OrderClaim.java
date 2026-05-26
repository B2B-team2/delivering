package com.sparta.operationsservice.claim.domain.core;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_order_claims")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID claimId;

    @Column(nullable = false)
    private UUID orderItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.REQUESTED;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    public void updateStatus(ClaimStatus status) {
        this.status = status;
    }

    public void updateStatusAndAmount(ClaimStatus status, BigDecimal refundAmount) {
        this.status = status;
        if (refundAmount != null) {
            this.refundAmount = refundAmount;
        }
    }

}
