import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

/**
 * GET /api/v1/deliveries/tracking/{tracking_number}
 * DeliveryService.trackDelivery() 는 @Cacheable(value = "deliveryTracking", key = "#trackingNumber") 로
 * Redis에 결과를 저장한다. DB 조회 + 정렬/가공 로직 전체를 캐싱하므로,
 * 위 route-cache-loadtest.js 보다 훨씬 "순수하게" 캐시 유무 효과를 보여준다
 * (외부 서비스 호출, DB insert 등 다른 변수가 없음).
 *
 * - hit 시나리오: 동일한 trackingNumber 반복 조회 -> 2번째 요청부터 Redis에서 바로 응답
 * - miss 시나리오: 매번 다른 trackingNumber 조회 (혹은 매 요청 전 강제 evict) -> 매번 DB까지 조회
 *
 * 사전 준비: TRACKING_NUMBERS 에 실제 존재하는 tracking_number를 콤마로 여러 개 넣어주세요.
 * (miss 시나리오는 캐시 안 탄 요청을 흉내내기 위해 존재하는 번호들을 매번 다르게 순회합니다)
 *
 * 실행 예)
 *   k6 run -e BASE_URL=http://localhost:8080 \
 *          -e TRACKING_NUMBERS=SL2507170001,SL2507170002,SL2507170003 \
 *          tracking-cache-loadtest.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TRACKING_NUMBERS = (__ENV.TRACKING_NUMBERS || 'SL2507170001').split(',');
const FIXED_TRACKING_NUMBER = TRACKING_NUMBERS[0];

const hitDuration = new Trend('tracking_duration_cache_hit', true);
const missDuration = new Trend('tracking_duration_cache_miss', true);

export const options = {
    scenarios: {
        cache_hit: {
            executor: 'constant-vus',
            exec: 'cacheHit',
            vus: Number(__ENV.VUS || 30),
            duration: __ENV.DURATION || '30s',
            startTime: '0s',
        },
        cache_miss: {
            executor: 'constant-vus',
            exec: 'cacheMiss',
            vus: Number(__ENV.VUS || 30),
            duration: __ENV.DURATION || '30s',
            startTime: __ENV.DURATION || '30s',
        },
    },
    thresholds: {
        'tracking_duration_cache_hit': ['p(95)<300'],
        'tracking_duration_cache_miss': ['p(95)<1000'],
    },
};

function get(trackingNumber, tagLabel) {
    const res = http.get(`${BASE_URL}/api/v1/deliveries/tracking/${trackingNumber}`, {
        tags: { cache_scenario: tagLabel },
    });
    const ok = check(res, { 'status is 200': (r) => r.status === 200 });
    if (!ok) {
        console.error(`[${tagLabel}] status=${res.status} body=${res.body}`);
    }
    return res;
}

// 항상 같은 번호 -> 최초 1회만 DB 조회, 이후 전부 Redis 캐시 HIT
export function cacheHit() {
    const res = get(FIXED_TRACKING_NUMBER, 'hit');
    hitDuration.add(res.timings.duration);
    sleep(0.1);
}

// 여러 번호를 랜덤하게 순회 -> 캐시가 워밍업되지 않은 키를 계속 만나게 해서 MISS 비율을 높임
// (완전한 MISS 보장을 원하면 테스트 전 Redis에서 deliveryTracking 캐시를 FLUSH 하거나
//  트래킹 번호 풀을 VU/iteration 수보다 훨씬 크게 준비하세요)
export function cacheMiss() {
    const trackingNumber = TRACKING_NUMBERS[Math.floor(Math.random() * TRACKING_NUMBERS.length)];
    const res = get(trackingNumber, 'miss');
    missDuration.add(res.timings.duration);
    sleep(0.1);
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data),
        'summary.json': JSON.stringify(data, null, 2),
    };
}

function textSummary(data) {
    const hit = data.metrics['tracking_duration_cache_hit'];
    const miss = data.metrics['tracking_duration_cache_miss'];
    const fmt = (m) => m ? `avg=${m.values.avg.toFixed(1)}ms p95=${m.values['p(95)'].toFixed(1)}ms max=${m.values.max.toFixed(1)}ms` : 'n/a';

    const checks = data.metrics['checks'];
    const checksPassRate = checks ? (checks.values.rate * 100).toFixed(2) : 'n/a';
    const httpFailed = data.metrics['http_req_failed'];
    const httpFailedRate = httpFailed ? (httpFailed.values.rate * 100).toFixed(2) : 'n/a';

    return `
==== 배송 조회(deliveryTracking Redis 캐시) 결과 요약 ====
CACHE HIT  (같은 트래킹번호 반복) : ${fmt(hit)}
CACHE MISS (여러 트래킹번호 순회) : ${fmt(miss)}
-----------------------------------------------------------
checks 통과율       : ${checksPassRate}%
http_req_failed 비율 : ${httpFailedRate}%
※ 100%가 아니라면 아래 [hit]/[miss] status=... 로그로 원인부터 확인하세요.
===========================================================
`;
}