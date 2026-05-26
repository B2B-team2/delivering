-- ============================================================
-- V2: p_order_drafts (장바구니) 테이블 추가
-- 스키마: order-db
-- ============================================================

CREATE TABLE IF NOT EXISTS "order-db".p_order_drafts
(
    draft_id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID         NOT NULL,
    product_id        UUID         NOT NULL,
    product_option_id UUID         NOT NULL,
    quantity          INTEGER      NOT NULL DEFAULT 1,
    created_at        TIMESTAMP    NOT NULL,
    created_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(255)
);
