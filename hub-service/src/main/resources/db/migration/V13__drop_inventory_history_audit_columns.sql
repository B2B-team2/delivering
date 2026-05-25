-- InventoryHistory는 append-only 설계 -> 불필요한 감사 컬럼 제거
ALTER TABLE "hub-db".p_inventory_histories
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS updated_by,
    DROP COLUMN IF EXISTS deleted_at,
    DROP COLUMN IF EXISTS deleted_by;
