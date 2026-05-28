SET search_path TO "delivery-db", public;

CREATE TABLE IF NOT EXISTS "p_delivery_log" (
    log_id         UUID          NOT NULL,
    delivery_id    UUID          NOT NULL,
    route_id       UUID          NOT NULL,
    event_type     VARCHAR(30)   NOT NULL,
    previous_value JSON,
    current_value  JSON,
    reason         TEXT,
    created_at                   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                   UUID,
    updated_at                   TIMESTAMP,
    updated_by                   UUID ,
    deleted_at                   TIMESTAMP,
    deleted_by                   UUID ,

    CONSTRAINT pk_p_delivery_log PRIMARY KEY (log_id)
    );

CREATE INDEX IF NOT EXISTS idx_delivery_log_delivery_id ON "p_delivery_log" (delivery_id);
CREATE INDEX IF NOT EXISTS idx_delivery_log_route_id ON "p_delivery_log" (route_id);