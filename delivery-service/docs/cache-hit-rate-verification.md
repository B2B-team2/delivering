# deliveryTracking 캐시 히트율 검증 결과

## 캐시 설정

`RedisCacheConfig` — `deliveryTracking` 캐시 TTL 2일(기본 캐시 TTL은 10분), 명시적 무효화는 `@CacheEvict`로 배송 취소/상태 변경/배차/배송 완료 시점마다 수행(TTL은 안전망, 실제 무효화는 쓰기 시점 즉시 처리).

## 치명적 버그 발견 — 캐시 히트가 항상 실패하고 있었음

측정을 위해 먼저 같은 tracking_number를 반복 조회했더니, **두 번째 호출(캐시 히트)부터 전부 500(→보안 필터를 거치며 403으로 응답)으로 실패**했다.

```
java.lang.ClassCastException: class java.util.LinkedHashMap cannot be cast to class
DeliveryTrackingResponse
```

**원인**: `RedisCacheConfig`가 `Jackson2JsonRedisSerializer<Object>`를 사용했는데, 이 직렬화 방식은 Redis에 저장하는 JSON에 타입 정보를 남기지 않는다. 캐시 히트 시 역직렬화하면 원래 타입이 아니라 `LinkedHashMap`으로 반환되고, Spring의 캐시 프록시가 이를 `DeliveryTrackingResponse`로 캐스팅하려다 매번 예외가 발생했다. **즉 캐시 히트가 발생하는 순간 요청이 죽는 구조**였다(1회성 조회만 하는 경우엔 드러나지 않는 버그).

## 수정

`Jackson2JsonRedisSerializer<Object>` → `GenericJackson2JsonRedisSerializer`로 교체하고, 커스텀 `ObjectMapper`에 `activateDefaultTyping()`을 명시적으로 호출해 JSON에 `@class` 타입 정보가 포함되도록 했다(Spring Data Redis 공식 문서에서 권장하는 표준 해법).

## 재검증 — 히트율 실측

동일한 tracking_number 5개를 각각 20회씩(총 100건) 순차 조회.

| 지표 | 값 |
|---|---|
| 총 요청 | 100건 |
| 성공 | 100건 (수정 전: 4건) |
| 미스(1회차, DB 조회) | 5건 |
| 히트(2회차 이후, Redis) | 95건 |
| **히트율** | **95.0%** |
| 미스 평균 지연시간 | 16.8ms |
| 히트 평균 지연시간 | 5.9ms |
| 히트가 미스 대비 | 2.8배 빠름 |

**Redis `INFO stats`로 교차 검증**: `keyspace_hits:95`, `keyspace_misses:5` — 애플리케이션 레벨 측정과 정확히 일치.

## 결론 (면접 답변용)

> "캐시 히트율을 실측하려고 같은 조회를 반복하는 테스트를 짰는데, 두 번째 호출부터 전부 실패하는 걸 발견했습니다. 원인을 추적하니 Redis 직렬화 방식이 타입 정보를 저장하지 않아서, 캐시 히트 시 역직렬화가 LinkedHashMap으로 떨어지며 ClassCastException이 나고 있었습니다. 즉 캐시가 있다고 문서화만 돼 있었을 뿐 실제로는 히트가 발생하는 순간 항상 죽는 상태였던 겁니다. GenericJackson2JsonRedisSerializer로 교체해 타입 정보를 포함시키고 재검증하니, 100건 중 100건 모두 성공했고 Redis 자체 통계로도 히트율 95%(캐시 미스 5건, 히트 95건)를 확인했습니다. 히트 시 응답이 미스 대비 2.8배 빨랐습니다. 이번에도 '캐시가 구현돼 있다'와 '캐시가 실제로 동작한다'는 다르다는 걸 실측으로 확인한 사례입니다."
