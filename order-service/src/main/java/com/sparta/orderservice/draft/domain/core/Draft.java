package com.sparta.orderservice.draft.domain.core;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.entity.BaseEntity;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_order_drafts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Draft extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_option_id", nullable = false)
    private UUID productOptionId;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;                        // 낙관적 락 — 동시 수량 수정/삭제 충돌 감지

    private static final int MAX_QUANTITY = 9999;

    public static Draft of(UUID userId, UUID productId, UUID productOptionId, int quantity) {
        Draft draft = new Draft();
        draft.userId = userId;
        draft.productId = productId;
        draft.productOptionId = productOptionId;
        draft.applyQuantity(quantity);
        return draft;
    }

    public void updateQuantity(int quantity) {
        applyQuantity(quantity);
    }

    // 삭제된 임시주문 항목을 복원하고 수량 갱신 (upsert 패턴)
    public void restore(int quantity) {
        this.clearDeleted();
        applyQuantity(quantity);
    }

    public void delete(UUID deletedBy) {
        this.softDelete(deletedBy);
    }

    // 모든 수량 변경 -> 공통 메서드를 통해 검증 후 적용
    private void applyQuantity(int quantity) {
        if (quantity <= 0 || quantity > MAX_QUANTITY) {
            throw new BusinessException(DraftErrorCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;
    }
}
