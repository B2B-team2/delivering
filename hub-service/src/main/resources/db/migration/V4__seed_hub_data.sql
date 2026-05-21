INSERT INTO "hub-db".p_logistics_hubs
    (hub_id, name, hub_type, address, latitude, longitude, contact_phone, status,
     created_at, created_by, updated_at, updated_by)
VALUES
    (gen_random_uuid(), '서울특별시 센터',    'REGIONAL', '서울특별시 송파구 송파대로 55',                  37.4742, 127.1236, '02-2000-0001', 'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '경기 북부 센터',     'REGIONAL', '경기도 고양시 덕양구 권율대로 570',             37.6403, 126.8737, '031-8075-0001', 'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '경기 남부 센터',     'CENTRAL',  '경기도 이천시 덕평로 257-21',                  37.1896, 127.3750, '031-8008-0001', 'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '인천광역시 센터',    'REGIONAL', '인천 남동구 정각로 29',                         37.4560, 126.7052, '032-440-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '강원특별자치도 센터','REGIONAL', '강원특별자치도 춘천시 중앙로 1',                37.8800, 127.7279, '033-249-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '대전광역시 센터',    'CENTRAL',  '대전 서구 둔산로 100',                         36.3503, 127.3846, '042-270-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '세종특별자치시 센터','REGIONAL', '세종특별자치시 한누리대로 2130',               36.4800, 127.2890, '044-300-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '충청북도 센터',      'REGIONAL', '충북 청주시 상당구 상당로 82',                 36.6353, 127.4914, '043-220-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '충청남도 센터',      'REGIONAL', '충남 홍성군 홍북읍 충남대로 21',               36.6590, 126.6730, '041-635-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '전북특별자치도 센터','REGIONAL', '전북특별자치도 전주시 완산구 효자로 225',      35.8194, 127.1063, '063-280-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '광주광역시 센터',    'REGIONAL', '광주 서구 내방로 111',                         35.1600, 126.8514, '062-608-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '전라남도 센터',      'REGIONAL', '전남 무안군 삼향읍 오룡길 1',                  34.8174, 126.4654, '061-286-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '대구광역시 센터',    'CENTRAL',  '대구 북구 태평로 161',                         35.8758, 128.5961, '053-803-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '경상북도 센터',      'REGIONAL', '경북 안동시 풍천면 도청대로 455',              36.5761, 128.5057, '054-880-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '경상남도 센터',      'REGIONAL', '경남 창원시 의창구 중앙대로 300',              35.2378, 128.6919, '055-211-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '부산광역시 센터',    'REGIONAL', '부산 동구 중앙대로 206',                       35.1176, 129.0450, '051-120-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), '울산광역시 센터',    'REGIONAL', '울산 남구 중앙로 201',                         35.5390, 129.3113, '052-120-0001',  'ACTIVE', NOW(), 'system', NOW(), 'system');
