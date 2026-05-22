-- ============================================================
-- V1: order-service 초기 스키마 생성
-- 스키마: order-db
-- 테이블: p_orders, p_company_orders, p_order_items, p_payments
-- ============================================================
CREATE SCHEMA IF NOT EXISTS "order-db";

-- ============================================================
-- 1. p_orders (주문)
-- ============================================================
CREATE TABLE IF NOT EXISTS "order-db".p_orders
(
    order_id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    requester_company_id UUID         NOT NULL,                 -- 요청(공급)업체
    receiver_company_id  UUID         NOT NULL,                 -- 수령업체
    recipient_name       VARCHAR(100) NOT NULL,                 -- 수령인 실명
    phone                VARCHAR(20)  NOT NULL,                 -- 수령인 연락처
    slack_id             VARCHAR(36),                           -- 수령인 Slack ID (nullable)
    address              JSONB        NOT NULL,                 -- 배송 주소 스냅샷
    due_date             TIMESTAMP    NOT NULL,                 -- 납품 기한
    request_memo         TEXT,                                  -- 요청 사항 (nullable)
    total_price          NUMERIC(12, 2) NOT NULL,               -- 상품 합계 금액
    delivery_fee         NUMERIC(8, 2)  NOT NULL DEFAULT 0,     -- 총 배송비
    final_price          NUMERIC(12, 2) NOT NULL,               -- 최종 결제 금액
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING', -- 주문 상태
    created_at           TIMESTAMP    NOT NULL,
    created_by           VARCHAR(255),
    updated_at           TIMESTAMP,
    updated_by           VARCHAR(255),
    deleted_at           TIMESTAMP,
    deleted_by           VARCHAR(255)
);

-- ============================================================
-- 2. p_company_orders (업체별 서브 주문)
-- ============================================================
CREATE TABLE IF NOT EXISTS "order-db".p_company_orders
(
    company_order_id      UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id              UUID          NOT NULL,               -- p_orders FK
    company_id            UUID          NOT NULL,               -- 공급업체
    subtotal_price        NUMERIC(12, 2) NOT NULL,              -- 업체별 상품 합계
    subtotal_delivery_fee NUMERIC(8, 2)  NOT NULL DEFAULT 0,    -- 업체별 배송비
    status                VARCHAR(30)   NOT NULL DEFAULT 'ORDERED', -- 서브 주문 상태
    created_at            TIMESTAMP     NOT NULL,
    created_by            VARCHAR(255),
    updated_at            TIMESTAMP,
    updated_by            VARCHAR(255),
    deleted_at            TIMESTAMP,
    deleted_by            VARCHAR(255),

    CONSTRAINT fk_company_order_order FOREIGN KEY (order_id) REFERENCES "order-db".p_orders (order_id)
);

-- ============================================================
-- 3. p_order_items (주문 항목)
-- ============================================================
CREATE TABLE IF NOT EXISTS "order-db".p_order_items
(
    order_item_id     UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    company_order_id  UUID          NOT NULL,                   -- p_company_orders FK
    delivery_id       UUID,                                     -- 배송 ID (nullable, 배송 생성 후 연결)
    product_option_id UUID          NOT NULL,                   -- 상품 옵션(SKU)
    quantity          INTEGER       NOT NULL,                   -- 구매 수량
    unit_price        NUMERIC(12, 2) NOT NULL,                  -- 구매 시점 단가
    created_at        TIMESTAMP     NOT NULL,
    created_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(255),

    CONSTRAINT fk_order_item_company_order FOREIGN KEY (company_order_id) REFERENCES "order-db".p_company_orders (company_order_id)
);

-- ============================================================
-- 4. p_payments (결제)
-- ============================================================
CREATE TABLE IF NOT EXISTS "order-db".p_payments
(
    payment_id        UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id          UUID          NOT NULL,                       -- p_orders FK
    payment_method    VARCHAR(30)   NOT NULL DEFAULT 'CARD',        -- 결제 방식 (CARD만 허용)
    amount            NUMERIC(12, 2),                               -- 결제 금액
    status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING',     -- 결제 상태 (PENDING, COMPLETED, CANCELLED)
    pg_transaction_id VARCHAR(255),                                 -- PG사 거래 ID (결제 승인 후 발급)
    created_at        TIMESTAMP     NOT NULL,
    created_by        VARCHAR(255),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(255),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(255),

    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES "order-db".p_orders (order_id)
);
