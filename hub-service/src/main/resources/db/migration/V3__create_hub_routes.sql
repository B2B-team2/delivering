CREATE TABLE "hub-db".p_hub_routes (
    route_id      UUID           PRIMARY KEY,
    from_hub_id   UUID           NOT NULL,
    to_hub_id     UUID           NOT NULL,
    duration      INTEGER        NOT NULL,
    distance      NUMERIC(10, 2) NOT NULL,
    created_at    TIMESTAMP      NOT NULL,
    created_by    VARCHAR(100),
    updated_at    TIMESTAMP,
    updated_by    VARCHAR(100),
    deleted_at    TIMESTAMP,
    deleted_by    VARCHAR(100)
);
