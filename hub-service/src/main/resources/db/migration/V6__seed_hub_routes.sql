SET search_path TO "hub-db", public;

-- 모든 허브 간 경로 자동 삽입
-- 직선거리 * 1.3 (도로 우회계수) = 실제 도로거리 추정
-- 평균 속도 60 km/h 기준으로 소요 시간 계산
INSERT INTO "hub-db".p_hub_routes
    (route_id, from_hub_id, to_hub_id, distance, duration, created_at, created_by)
SELECT
    gen_random_uuid(),
    h1.hub_id,
    h2.hub_id,
    -- 거리 (km): 직선거리 * 도로우회계수 1.3
    ROUND((ST_Distance(h1.location::geography, h2.location::geography) / 1000 * 1.3)::numeric, 2),
    -- 시간 (분): 거리(km) / 60(km/h) * 60(min) = 거리(km) 분, 최소 10분
    GREATEST(10, ROUND((ST_Distance(h1.location::geography, h2.location::geography) / 1000 * 1.3))::int),
    NOW(),
    'system'
FROM "hub-db".p_logistics_hubs h1
CROSS JOIN "hub-db".p_logistics_hubs h2
WHERE h1.hub_id <> h2.hub_id
  AND h1.deleted_at IS NULL
  AND h2.deleted_at IS NULL;
