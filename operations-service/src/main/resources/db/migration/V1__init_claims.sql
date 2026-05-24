-- operation_db 스키마 생성 및 검색 경로 설정
CREATE SCHEMA IF NOT EXISTS "operation-db";
SET search_path TO "operation-db", public;

-- p_order_claims (반품/교환) 테이블 생성
CREATE TABLE p_order_claims (
    claim_id UUID PRIMARY KEY,
    order_item_id UUID NOT NULL,
    claim_type VARCHAR(30) NOT NULL, -- RETURN, EXCHANGE
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, PROCESSING, REJECTED, COMPLETED, CANCELLED
    reason TEXT NOT NULL,
    refund_amount NUMERIC(12,2) DEFAULT 0,
    
    -- BaseEntity 필드
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- 인덱스 추가
CREATE INDEX idx_p_order_claims_order_item_id ON p_order_claims(order_item_id);
CREATE INDEX idx_p_order_claims_deleted_at ON p_order_claims(deleted_at);
