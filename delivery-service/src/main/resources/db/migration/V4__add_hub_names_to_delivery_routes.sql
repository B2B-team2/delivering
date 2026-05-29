SET search_path TO "delivery-db", public;

ALTER TABLE "p_delivery_routes"
    ADD COLUMN from_hub_name VARCHAR(100),
ADD COLUMN to_hub_name VARCHAR(100);