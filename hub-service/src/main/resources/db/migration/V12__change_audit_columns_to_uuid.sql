-- hub-service 테이블의 audit 컬럼을 UUID로 변경 (기존 문자열을 UUID로 캐스팅)

ALTER TABLE "hub-db".p_logistics_hubs 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "hub-db".p_hub_routes 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "hub-db".p_warehouses 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "hub-db".p_warehouse_inventory 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);

ALTER TABLE "hub-db".p_inventory_histories 
    ALTER COLUMN created_by TYPE UUID USING (NULLIF(created_by, '')::UUID),
    ALTER COLUMN updated_by TYPE UUID USING (NULLIF(updated_by, '')::UUID),
    ALTER COLUMN deleted_by TYPE UUID USING (NULLIF(deleted_by, '')::UUID);
