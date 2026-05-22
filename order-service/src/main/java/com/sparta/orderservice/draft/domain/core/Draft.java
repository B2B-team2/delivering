package com.sparta.orderservice.draft.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    public static Draft of(UUID userId, UUID productId, UUID productOptionId, int quantity) {
        Draft draft = new Draft();
        draft.userId = userId;
        draft.productId = productId;
        draft.productOptionId = productOptionId;
        draft.quantity = quantity;
        return draft;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    // 삭제된 임시주문 항목을 복원하고 수량 갱신 (upsert 패턴)
    public void restore(int quantity) {
        this.clearDeleted();
        this.quantity = quantity;
    }

    public void delete(String deletedBy) {
        this.softDelete(deletedBy);
    }
}
