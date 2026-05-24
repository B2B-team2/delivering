-- company-service 테이블의 audit 컬럼을 UUID로 변경 (기존 데이터가 UUID 형태가 아닌 문자열이면 초기화 또는 캐스팅)
-- PostgreSQL의 경우 USING 절을 사용하여 기존 값을 UUID로 캐스팅합니다.

ALTER TABLE p_product_categories 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE p_companies 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE p_products 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE p_product_options 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE p_delivery_addresses 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);
