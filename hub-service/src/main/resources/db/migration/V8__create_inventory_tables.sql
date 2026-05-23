CREATE TABLE "hub-db".p_warehouse_inventory (
    inventory_id       UUID         PRIMARY KEY,
    warehouse_id       UUID         NOT NULL,
    product_option_id  UUID         NOT NULL,
    quantity           INTEGER      NOT NULL DEFAULT 0,
    reserved_quantity  INTEGER      NOT NULL DEFAULT 0,
    safety_stock       INTEGER      NOT NULL DEFAULT 0,
    version            BIGINT                DEFAULT 0,
    created_at         TIMESTAMP    NOT NULL,
    created_by         VARCHAR(100),
    updated_at         TIMESTAMP,
    updated_by         VARCHAR(100),
    deleted_at         TIMESTAMP,
    deleted_by         VARCHAR(100),
    UNIQUE (warehouse_id, product_option_id)
);

CREATE TABLE "hub-db".p_inventory_histories (
    history_id       UUID         PRIMARY KEY,
    inventory_id     UUID         NOT NULL REFERENCES "hub-db".p_warehouse_inventory(inventory_id),
    change_quantity  INTEGER      NOT NULL,
    change_type      VARCHAR(30)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(100),
    updated_at       TIMESTAMP,
    updated_by       VARCHAR(100),
    deleted_at       TIMESTAMP,
    deleted_by       VARCHAR(100)
);
