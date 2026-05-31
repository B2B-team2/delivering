CREATE TABLE "hub-db".p_logistics_hubs (
    hub_id        UUID         PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    hub_type      VARCHAR(20),
    address       TEXT         NOT NULL,
    latitude      FLOAT8       NOT NULL,
    longitude     FLOAT8       NOT NULL,
    contact_phone VARCHAR(50),
    status        VARCHAR(20),
    created_at    TIMESTAMP    NOT NULL,
    created_by    VARCHAR(100),
    updated_at    TIMESTAMP,
    updated_by    VARCHAR(100),
    deleted_at    TIMESTAMP,
    deleted_by    VARCHAR(100)
);
