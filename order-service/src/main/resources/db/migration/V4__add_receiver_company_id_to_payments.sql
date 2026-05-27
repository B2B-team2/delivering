-- p_payments 테이블에 receiver_company_id 반정규화
-- getPayments COMPANY_MANAGER 필터링 시 IN 쿼리 없이 단일 쿼리로 처리하기 위함

ALTER TABLE "order-db".p_payments
    ADD COLUMN IF NOT EXISTS receiver_company_id UUID;

-- 기존 데이터 backfill: p_orders에서 receiver_company_id 조회
UPDATE "order-db".p_payments p
SET receiver_company_id = o.receiver_company_id
FROM "order-db".p_orders o
WHERE p.order_id = o.order_id;

ALTER TABLE "order-db".p_payments
    ALTER COLUMN receiver_company_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payments_receiver_company_id
    ON "order-db".p_payments (receiver_company_id)
    WHERE deleted_at IS NULL;
