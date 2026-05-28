CREATE SCHEMA IF NOT EXISTS "operation-db";
SET search_path TO "operation-db", public;

CREATE TABLE IF NOT EXISTS "p_ai_requests" (
    request_id          UUID          NOT NULL,
    user_id             UUID          NOT NULL,
    delivery_id         UUID,
    ai_model_name       VARCHAR(100)  NOT NULL,
    prompt_text         TEXT          NOT NULL,
    response_text       TEXT,
    status              VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    error_message       TEXT,
    final_deadline_at   TIMESTAMP,

    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    deleted_at          TIMESTAMP,
    deleted_by          UUID,

    -- 제약 조건 설정 (PK)
    CONSTRAINT pk_p_ai_requests PRIMARY KEY (request_id)
    );

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_ai_requests_user_id ON "p_ai_requests" (user_id);
CREATE INDEX IF NOT EXISTS idx_ai_requests_delivery_id ON "p_ai_requests" (delivery_id);
CREATE INDEX IF NOT EXISTS idx_ai_requests_status ON "p_ai_requests" (status);