-- operation-service 테이블의 audit 컬럼을 UUID로 변경 (기존 문자열을 UUID로 캐스팅)
-- 'system' 등 UUID 형식이 아닌 데이터는 고정된 시스템 UUID(00000000-0000-0000-0000-000000000000)로 변환합니다.

CREATE OR REPLACE FUNCTION convert_to_uuid(v_input text) RETURNS uuid AS $$
BEGIN
    IF v_input IS NULL OR v_input = '' THEN
        RETURN NULL;
    END IF;
    IF v_input ~ '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$' THEN
        RETURN v_input::uuid;
    ELSE
        RETURN '00000000-0000-0000-0000-000000000000'::uuid;
    END IF;
EXCEPTION WHEN OTHERS THEN
    RETURN '00000000-0000-0000-0000-000000000000'::uuid;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE p_order_claims
    ALTER COLUMN created_by TYPE UUID USING convert_to_uuid(created_by),
    ALTER COLUMN updated_by TYPE UUID USING convert_to_uuid(updated_by),
    ALTER COLUMN deleted_by TYPE UUID USING convert_to_uuid(deleted_by);

DROP FUNCTION convert_to_uuid(text);
