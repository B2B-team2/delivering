/**
 * deliveryTracking 캐시 히트율 측정 테스트
 *
 * 방법: 서로 다른 tracking_number N개를 각각 20회씩 순차 조회한다.
 * 각 tracking_number의 1회차 호출은 캐시 미스(DB 조회), 2~20회차는 캐시 히트(Redis)가 기대된다.
 * 이론상 미스율 = N / (N*20) = 5%.
 *
 * 실행: k6 run --env BASE_URL=http://localhost:19095 \
 *              --env GATEWAY_SECRET=<secret> \
 *              --env TRACKING_NUMBERS=SL2609220001,SL2609220002,... \
 *              cache-hit-rate-test.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:19095';
const GATEWAY_SECRET = __ENV.GATEWAY_SECRET;
const TRACKING_NUMBERS = (__ENV.TRACKING_NUMBERS || '').split(',').filter(Boolean);
const REPEATS_PER_NUMBER = parseInt(__ENV.REPEATS_PER_NUMBER || '20');

const firstCallLatency = new Trend('first_call_latency_ms'); // 캐시 미스(기대)
const repeatCallLatency = new Trend('repeat_call_latency_ms'); // 캐시 히트(기대)
const okCount = new Counter('ok_count');
const failCount = new Counter('fail_count');

export const options = {
    scenarios: {
        cache_hit_rate: {
            executor: 'shared-iterations',
            vus: 1, // 순차 실행(같은 순서로 첫 호출/반복 호출을 명확히 구분하기 위해)
            iterations: 1,
            maxDuration: '2m',
        },
    },
};

export default function () {
    for (const trackingNumber of TRACKING_NUMBERS) {
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
    const numbers = TRACKING_NUMBERS.length;
    const expectedMisses = numbers;
    const expectedHits = total - expectedMisses;

    console.log('\n===== 캐시 히트율 테스트 결과 =====');
    console.log(`tracking_number 종류: ${numbers}개, 번호당 반복: ${REPEATS_PER_NUMBER}회, 총 요청: ${total}건`);
    console.log(`성공: ${ok}건, 실패: ${fail}건`);
    console.log(`이론상 미스(1회차): ${expectedMisses}건, 이론상 히트(2회차 이후): ${expectedHits}건`);
    console.log(`이론상 히트율: ${((expectedHits / total) * 100).toFixed(1)}%`);
    if (first) {
        console.log(`\n1회차(미스 기대) 지연시간: avg=${first.avg.toFixed(1)}ms p(95)=${first['p(95)'].toFixed(1)}ms`);
    }
    if (repeat) {
        console.log(`2회차+(히트 기대) 지연시간: avg=${repeat.avg.toFixed(1)}ms p(95)=${repeat['p(95)'].toFixed(1)}ms`);
    }
    if (first && repeat) {
        const speedup = (first.avg / repeat.avg).toFixed(1);
        console.log(`\n캐시 히트가 미스 대비 ${speedup}배 빠름`);
    }

    return {};
}
