-- 기존 데이터가 있으면 NOT NULL 컬럼 추가가 불가능하므로 데이터를 비우고 진행합니다.
TRUNCATE TABLE p_order_claims;

ALTER TABLE p_order_claims DROP COLUMN IF EXISTS order_item_id;
ALTER TABLE p_order_claims ADD COLUMN company_order_id UUID NOT NULL;
