SET search_path TO "delivery-db", public;

CREATE TABLE IF NOT EXISTS "p_delivery_routes" (
    route_id            UUID          NOT NULL,
    delivery_id         UUID          NOT NULL,
    sequence            INTEGER       NOT NULL,
    from_hub_id         UUID          NOT NULL,
    to_hub_id           UUID          NOT NULL,
    estimated_distance  NUMERIC(8,2),
    estimated_duration  Time,
    actual_distance     NUMERIC(8,2),
    actual_duration     Time,
    status              VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100)  NOT NULL DEFAULT 'SYSTEM',
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(100),
    deleted_at          TIMESTAMP,
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_p_delivery_routes PRIMARY KEY (route_id)
    );

CREATE INDEX IF NOT EXISTS idx_delivery_routes_delivery_id ON "p_delivery_routes" (delivery_id);
CREATE INDEX IF NOT EXISTS idx_delivery_routes_status ON "p_delivery_routes" (status);