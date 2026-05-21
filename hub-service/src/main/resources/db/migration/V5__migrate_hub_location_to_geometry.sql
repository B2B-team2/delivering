-- search_path에 public 포함 (PostGIS 타입/함수 접근용)
SET search_path TO "hub-db", public;

-- PostGIS 익스텐션 활성화
CREATE EXTENSION IF NOT EXISTS postgis SCHEMA public;

-- location 컬럼 추가 (nullable로 먼저 추가)
ALTER TABLE "hub-db".p_logistics_hubs
    ADD COLUMN location GEOMETRY(Point, 4326);

-- 기존 latitude/longitude → location 변환 (ST_MakePoint: x=longitude, y=latitude)
UPDATE "hub-db".p_logistics_hubs
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326);

-- NOT NULL 제약 적용
ALTER TABLE "hub-db".p_logistics_hubs
    ALTER COLUMN location SET NOT NULL;

-- 기존 컬럼 제거
ALTER TABLE "hub-db".p_logistics_hubs
    DROP COLUMN latitude,
    DROP COLUMN longitude;
