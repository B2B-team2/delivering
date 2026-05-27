ALTER TABLE operation_db.p_order_claims DROP COLUMN order_item_id;
ALTER TABLE operation_db.p_order_claims ADD COLUMN company_order_id UUID NOT NULL;
