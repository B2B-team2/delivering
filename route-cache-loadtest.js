import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/**
 * 배송 생성(POST /api/v1/internal/deliveries) 시 내부에서 호출되는
 * cachedHubServiceClient.getHubRouteWithCache() 의 Redis 캐시 효과를 측정한다.
 *
 * - hit 시나리오: 고정된 fromHub/toHub 쌍을 반복 요청 -> 2번째 요청부터 Redis 캐시 HIT
 * - miss 시나리오: 매 요청마다 다른 fromHub/toHub 쌍 -> 매번 Hub 서비스 호출 (캐시 MISS)
 *
 * companyOrderId 는 중복 체크가 있으므로 매 요청 uuid로 유니크하게 생성한다.
 *
 * 실행 예)
 *   k6 run -e BASE_URL=http://localhost:8080 route-cache-loadtest.js
 *
 * 캐시 ON/OFF 진짜 비교(권장)를 하려면:
 *   1) application.yml 에서 spring.cache.type: redis 로 띄운 상태로 위 명령 실행 -> summary_cache_on.json
 *   2) spring.cache.type: none (또는 캐시 빈을 NoOpCacheManager로) 으로 띄운 상태로 동일 명령 재실행 -> summary_cache_off.json
 *   3) 두 JSON의 hit 시나리오 p95/avg 를 비교 (miss 시나리오는 어차피 캐시 유무와 무관하게 비슷해야 정상)
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 시나리오별 커스텀 지표 (기본 http_req_duration은 scenario 태그로 자동 분리되지만,
// 콘솔에서 바로 보기 편하도록 별도 Trend도 둔다)
const hitDuration = new Trend('route_create_duration_cache_hit', true);
const missDuration = new Trend('route_create_duration_cache_miss', true);
const failCount = new Counter('route_create_failures');

// 캐시 HIT 시나리오용 고정 허브 쌍 (실제 존재하는 hubId로 교체해서 사용)
const FIXED_FROM_HUB = __ENV.FIXED_FROM_HUB || '11111111-1111-1111-1111-111111111111';
const FIXED_TO_HUB = __ENV.FIXED_TO_HUB || '22222222-2222-2222-2222-222222222222';

// 캐시 MISS 시나리오용 - 실제 DB에 존재하는 허브 UUID 풀에서 매번 랜덤 조합
const HUB_POOL = (__ENV.HUB_POOL ||
    '33333333-3333-3333-3333-333333333333,44444444-4444-4444-4444-444444444444,55555555-5555-5555-5555-555555555555,66666666-6666-6666-6666-666666666666,77777777-7777-7777-7777-777777777777'
).split(',');

function randomHubPair() {
    const a = HUB_POOL[Math.floor(Math.random() * HUB_POOL.length)];
    let b = HUB_POOL[Math.floor(Math.random() * HUB_POOL.length)];
    while (b === a) b = HUB_POOL[Math.floor(Math.random() * HUB_POOL.length)];
    return [a, b];
}

function buildPayload(fromHubId, toHubId) {
    return [{
        companyOrderId: uuidv4(),
        companyReceiveId: uuidv4(),
        memo: 'k6 load test',
        departureHubId: fromHubId,
        destinationHubId: toHubId,
        deliveryAddress: {
            address: '서울시 강남구 테헤란로 1',
            addressDetail: '1층',
        },
        recipientName: 'k6 tester',
        phone: '01000000000',
        postalCode: '06236',
        recipientSlackId: null,
    }];
}

function post(payload, tagLabel) {
    const res = http.post(
        `${BASE_URL}/api/v1/internal/deliveries`,
        JSON.stringify(payload),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { cache_scenario: tagLabel },
        }
    );

    const ok = check(res, {
        'status is 200': (r) => r.status === 200,
    });
    if (!ok) {
        failCount.add(1);
        // 디버깅용: 실패 응답의 상태코드와 본문을 그대로 출력 (원인 파악 후 지워도 됩니다)
        console.error(`[${tagLabel}] status=${res.status} body=${res.body}`);
    }
    return res;
}

export const options = {
    scenarios: {
        cache_hit: {
            executor: 'constant-vus',
            exec: 'cacheHit',
            vus: Number(__ENV.VUS || 20),
            duration: __ENV.DURATION || '30s',
            startTime: '0s',
        },
        cache_miss: {
            executor: 'constant-vus',
            exec: 'cacheMiss',
            vus: Number(__ENV.VUS || 20),
            duration: __ENV.DURATION || '30s',
            // hit 시나리오가 끝난 뒤 이어서 실행 (동시 실행 시 리소스 경합으로 비교가 왜곡됨)
            startTime: __ENV.DURATION || '30s',
        },
    },
    thresholds: {
        'route_create_duration_cache_hit': ['p(95)<800'],
        'route_create_duration_cache_miss': ['p(95)<2000'],
    },
};

export function cacheHit() {
    const payload = buildPayload(FIXED_FROM_HUB, FIXED_TO_HUB);
    const res = post(payload, 'hit');
    hitDuration.add(res.timings.duration);
    sleep(0.2);
}

export function cacheMiss() {
    const [from, to] = randomHubPair();
    const payload = buildPayload(from, to);
    const res = post(payload, 'miss');
    missDuration.add(res.timings.duration);
    sleep(0.2);
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data),
        'summary.json': JSON.stringify(data, null, 2),
    };
}

function textSummary(data) {
    const hit = data.metrics['route_create_duration_cache_hit'];
    const miss = data.metrics['route_create_duration_cache_miss'];
    const fmt = (m) => m ? `avg=${m.values.avg.toFixed(1)}ms p95=${m.values['p(95)'].toFixed(1)}ms max=${m.values.max.toFixed(1)}ms` : 'n/a';

    const checks = data.metrics['checks'];
    const checksPassRate = checks ? (checks.values.rate * 100).toFixed(2) : 'n/a';
    const checksPasses = checks ? checks.values.passes : 'n/a';
    const checksFails = checks ? checks.values.fails : 'n/a';

    const httpFailed = data.metrics['http_req_failed'];
    const httpFailedRate = httpFailed ? (httpFailed.values.rate * 100).toFixed(2) : 'n/a';

    const failCounter = data.metrics['route_create_failures'];
    const failCount = failCounter ? failCounter.values.count : 0;

    const reqs = data.metrics['http_reqs'];
    const totalReqs = reqs ? reqs.values.count : 'n/a';

    return `
==== 배송 생성(허브 경로 Redis 캐시) 결과 요약 ====
CACHE HIT  (고정 허브쌍 반복) : ${fmt(hit)}
CACHE MISS (매번 다른 허브쌍) : ${fmt(miss)}
-----------------------------------------------------
총 요청 수         : ${totalReqs}
checks 통과율       : ${checksPassRate}% (pass=${checksPasses}, fail=${checksFails})
http_req_failed 비율 : ${httpFailedRate}%
route_create_failures : ${failCount}건 (200이 아닌 응답 수)
-----------------------------------------------------
※ checks 통과율이 100%가 아니거나 http_req_failed 비율이 0%가 아니면,
   위 avg/p95는 "정상 처리 속도"가 아니라 "에러 응답 속도"일 수 있습니다.
   콘솔에 찍힌 [hit]/[miss] status=... body=... 로그를 확인하세요.
=====================================================
`;
}