-- p_delivery_addresses 테이블의 address_id 타입을 VARCHAR에서 UUID로 변경
SET search_path TO "company-db", public;

ALTER TABLE p_delivery_addresses 
ALTER COLUMN address_id TYPE UUID USING address_id::UUID;
