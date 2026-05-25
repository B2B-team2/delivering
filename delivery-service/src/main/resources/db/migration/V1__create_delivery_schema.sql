CREATE SCHEMA IF NOT EXISTS "delivery-db";

SET search_path TO "delivery-db", public;

CREATE TABLE IF NOT EXISTS "p_deliveries" (
    delivery_id                  UUID          NOT NULL,
    company_order_id             UUID          NOT NULL,

    tracking_number              VARCHAR(100),
    status                       VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    memo                         TEXT,

    -- 허브 및 주소 정보
    departure_hub_id             UUID          NOT NULL,
    destination_hub_id           UUID          NOT NULL,
    delivery_address             VARCHAR(255)  NOT NULL,
    postal_code                  VARCHAR(5)    NOT NULL,

    -- 수령인 정보
    recipient_name               VARCHAR(100)  NOT NULL,
    phone                        VARCHAR(20)   NOT NULL,
    recipient_slack_id           VARCHAR(100),

    -- 배송 담당자 정보
    delivery_manager_id          UUID          NOT NULL,
    manager_name                 VARCHAR(100)  NOT NULL,
    manager_phone                VARCHAR(20)   NOT NULL,

    -- 배송 시간 관련 정보
    final_dispatch_deadline_at  TIMESTAMP,
    started_at                   TIMESTAMP,
    completed_at                 TIMESTAMP,

    -- 기타 외래키 참조
    company_receive_id           UUID          NOT NULL,

    -- 프로젝트 공통 메타데이터 컬럼
    created_at                   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                   VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
    updated_at                   TIMESTAMP,
    updated_by                   VARCHAR(100),
    deleted_at                   TIMESTAMP,
    is_deleted                   BOOLEAN       NOT NULL DEFAULT FALSE,

    -- 제약 조건 설정 (PK)
    CONSTRAINT pk_p_deliveries PRIMARY KEY (delivery_id)
    );

-- 4. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_deliveries_order_id ON "p_deliveries" (company_order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON "p_deliveries" (status);