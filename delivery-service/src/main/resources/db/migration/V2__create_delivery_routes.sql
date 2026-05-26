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
    created_at                   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                   UUID,
    updated_at                   TIMESTAMP,
    updated_by                   UUID ,
    deleted_at                   TIMESTAMP,
    deleted_by                   UUID ,
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_p_delivery_routes PRIMARY KEY (route_id)
    );

CREATE INDEX IF NOT EXISTS idx_delivery_routes_delivery_id ON "p_delivery_routes" (delivery_id);
CREATE INDEX IF NOT EXISTS idx_delivery_routes_status ON "p_delivery_routes" (status);