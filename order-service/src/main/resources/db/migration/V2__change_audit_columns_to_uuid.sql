-- order-service 테이블의 audit 컬럼을 UUID로 변경 (기존 문자열을 UUID로 캐스팅)

ALTER TABLE "order-db".p_orders 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "order-db".p_company_orders 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "order-db".p_order_items 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "order-db".p_payments 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);
