/**
 * deliveryTracking 캐시 히트율 측정 테스트 (대용량)
 *
 * 방법: TRACKING_PREFIX + 순번으로 생성되는 tracking_number NUM_KEYS개를 VUS개의 가상유저가
 * 나눠 맡아(파티션), 각자 담당 번호를 REPEATS_PER_NUMBER회씩 반복 조회한다.
 * 번호 하나는 항상 같은 VU만 건드리므로 VU 간 캐시 경쟁 없이 히트/미스를 명확히 구분한다.
 * 총 요청 수 = NUM_KEYS * REPEATS_PER_NUMBER (기본 1000 * 20 = 20,000건).
 * 이론상 미스율 = NUM_KEYS / 총요청.
 *
 * 실행: k6 run --env BASE_URL=http://localhost:19095 \
 *              --env GATEWAY_SECRET=<secret> \
 *              --env TRACKING_PREFIX=SLBULK --env NUM_KEYS=1000 \
 *              --env REPEATS_PER_NUMBER=20 --env VUS=20 \
 *              cache-hit-rate-test.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:19095';
const GATEWAY_SECRET = __ENV.GATEWAY_SECRET;
const TRACKING_PREFIX = __ENV.TRACKING_PREFIX || 'SLBULK';
const NUM_KEYS = parseInt(__ENV.NUM_KEYS || '1000');
const REPEATS_PER_NUMBER = parseInt(__ENV.REPEATS_PER_NUMBER || '20');
const VUS = parseInt(__ENV.VUS || '20');

const firstCallLatency = new Trend('first_call_latency_ms');
const repeatCallLatency = new Trend('repeat_call_latency_ms');
const okCount = new Counter('ok_count');
const failCount = new Counter('fail_count');

function trackingNumberOf(index) {
    return TRACKING_PREFIX + String(index).padStart(6, '0');
}

export const options = {
    scenarios: {
        cache_hit_rate: {
            executor: 'shared-iterations',
            vus: VUS,
            iterations: VUS,
            maxDuration: '5m',
        },
    },
};

export default function () {
    // __VU: 1..VUS. 이 VU가 담당할 키 인덱스(1..NUM_KEYS)를 VUS개로 균등 분할.
    for (let idx = __VU; idx <= NUM_KEYS; idx += VUS) {
        const trackingNumber = trackingNumberOf(idx);
        for (let i = 0; i < REPEATS_PER_NUMBER; i++) {
            const start = Date.now();
            const res = http.get(`${BASE_URL}/api/v1/deliveries/tracking/${trackingNumber}`, {
                headers: {
                    'X-Gateway-Secret': GATEWAY_SECRET,
                    'X-User-Id': '00000000-0000-0000-0000-000000000001',
                    'X-User-Role': 'MASTER',
                },
            });
            const elapsed = Date.now() - start;

            const ok = check(res, { 'status 200': (r) => r.status === 200 });
            ok ? okCount.add(1) : failCount.add(1);

            if (i === 0) {
                firstCallLatency.add(elapsed);
            } else {
                repeatCallLatency.add(elapsed);
            }
        }
    }
}

export function handleSummary(data) {
    const first = data.metrics['first_call_latency_ms']?.values;
    const repeat = data.metrics['repeat_call_latency_ms']?.values;
    const ok = data.metrics['ok_count'] ? data.metrics['ok_count'].values.count : 0;
    const fail = data.metrics['fail_count'] ? data.metrics['fail_count'].values.count : 0;
    const total = ok + fail;
    const expectedMisses = NUM_KEYS;
    const expectedHits = total - expectedMisses;
    const durationMs = data.state?.testRunDurationMs ?? 0;

    console.log('\n===== 캐시 히트율 테스트 결과 (대용량) =====');
    console.log(`동시 VU: ${VUS}, tracking_number 종류: ${NUM_KEYS}개, 번호당 반복: ${REPEATS_PER_NUMBER}회, 총 요청: ${total}건`);
    console.log(`성공: ${ok}건, 실패: ${fail}건`);
    console.log(`이론상 미스(1회차): ${expectedMisses}건, 이론상 히트(2회차 이후): ${expectedHits}건`);
    console.log(`이론상 히트율: ${((expectedHits / total) * 100).toFixed(2)}%`);
    if (first) {
        console.log(`\n미스 지연시간: avg=${first.avg.toFixed(1)}ms p(95)=${first['p(95)'].toFixed(1)}ms`);
    }
    if (repeat) {
        console.log(`히트 지연시간: avg=${repeat.avg.toFixed(1)}ms p(95)=${repeat['p(95)'].toFixed(1)}ms`);
    }
    if (first && repeat) {
        console.log(`\n캐시 히트가 미스 대비 ${(first.avg / repeat.avg).toFixed(1)}배 빠름`);
    }
    if (durationMs > 0) {
        console.log(`처리시간: ${(durationMs / 1000).toFixed(2)}s, TPS: ${(total / (durationMs / 1000)).toFixed(1)}`);
    }

    return {};
}
