CREATE TABLE "hub-db".p_warehouses (
    warehouse_id  UUID         PRIMARY KEY,
    hub_id        UUID         NOT NULL UNIQUE,
    warehouse_name VARCHAR(100),
    address       TEXT,
    region        VARCHAR(100),
    contact_phone VARCHAR(50),
    status        VARCHAR(20),
    created_at    TIMESTAMP    NOT NULL,
    created_by    VARCHAR(100),
    updated_at    TIMESTAMP,
    updated_by    VARCHAR(100),
    deleted_at    TIMESTAMP,
    deleted_by    VARCHAR(100)
);
