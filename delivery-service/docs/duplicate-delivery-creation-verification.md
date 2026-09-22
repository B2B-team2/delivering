# 배송 중복 생성 검증 결과

## 배경

동일한 `companyOrderId`로 동시에 여러 건의 배송 생성 요청이 들어올 때 중복 생성을 막는 장치가 실제로 동작하는지 검증했다.

## 원인 분석

- `DeliveryLockFacade`(Redisson 분산 락, `lock:order:{companyOrderId}`)가 이미 구현돼 있었지만, 실제 요청 경로인 `DeliveryInternalController` → `DeliveryService.createDelivery()`가 이 락을 **거치지 않고** 호출되고 있었다(완전히 미사용 상태의 죽은 코드).
- 유일한 중복 방지 장치는 `DeliveryService.createSingleDelivery()` 안의 `deliveryRepository.existsByCompanyOrderId(...)` 체크뿐이었다.
- `Delivery` 엔티티의 `company_order_id` 컬럼에는 DB 유니크 제약도 없어, 락도 DB 제약도 없는 상태로 check-then-act 경쟁 조건에 그대로 노출돼 있었다.

## 1차 검증 — 락 미적용 상태에서 중복 발생 재현 (동시성 50~1000 램핑)

동일 `companyOrderId`로 N개 스레드가 동시에 `DeliveryService.createSingleDelivery()`를 직접 호출(컨트롤러가 실제로 하던 것과 동일한 경로)하도록 테스트를 작성했다. DB 존재 체크는 실제 DB 왕복 지연을 흉내낸 30ms 지연을 준 Fake로 대체해 경쟁 조건을 재현했다. N을 50 → 1000까지 올려가며 재현 여부를 확인했다.

| 동시 요청 수 | 중복 생성(성공) | 정상 거부 | 중복 발생률 |
|---|---|---|---|
| 50 | 19 | 31 | 38% |
| 100 | 43 | 57 | 43% |
| 300 | 15 | 285 | 5% |
| 500 | 28 | 472 | 5.6% |
| 1000 | 33 | 967 | 3.3% |

**모든 구간에서 중복 생성이 실제로 발생함을 확인.** 다만 중복 발생률 자체는 N이 커질수록 단조 증가하지 않고 오히려 낮아지는 경향을 보였다 — 이는 테스트가 30ms의 고정 지연으로 경쟁 창을 흉내낸 방식이라, 스레드 수가 코어 수를 훨씬 넘어서면 OS 스케줄러가 스레드를 순차적으로 시분할하면서 "동시에 체크하는" 창 자체가 좁아지기 때문으로 보인다(로컬 머신의 하드웨어 병렬성 한계). 실제 운영 환경(수십 ms대 실제 DB 왕복 지연, 더 많은 코어)에서는 이 창이 테스트보다 더 넓게 유지되어 중복 발생률이 더 높을 가능성이 있다 — 즉 이 수치는 실제 위험도의 하한선에 가깝다.

## 수정

`DeliveryInternalController`가 `DeliveryService` 대신 이미 구현돼 있던 `DeliveryLockFacade.createDeliveriesWithLock()`을 호출하도록 변경(1줄 의존성 교체 + 메서드 호출 변경).

```diff
- List<DeliveryCreateResponse> response = deliveryService.createDelivery(requests);
+ List<DeliveryCreateResponse> response = deliveryLockFacade.createDeliveriesWithLock(requests);
```

## 2차 검증 — 락 적용 후 재실행 (동시성 50~1000 램핑)

같은 시나리오를 실제 Redis(delivering 자체 컨테이너, 로컬 26379)에 연결된 Redisson 락으로 재실행.

| 동시 요청 수 | 생성 성공 | 정상 거부 |
|---|---|---|
| 50 | 1 | 49 |
| 100 | 1 | 99 |
| 300 | 1 | 299 |
| 500 | 1 | 499 |
| 1000 | 1 | 999 |

**동시성 50~1000 전 구간에서 예외 없이 정확히 1건만 성공.** 락 적용 전에는 N에 따라 15~43건씩 중복이 발생했던 것과 대비된다.

## 결론 (면접 답변용)

> "배송 생성 시 중복 방지를 위해 Redisson 분산 락(`DeliveryLockFacade`)이 이미 구현돼 있었지만, 실제 컨트롤러가 이 락을 거치지 않고 서비스 메서드를 직접 호출하고 있다는 걸 코드 추적으로 발견했습니다. 동시성 50에서 1000까지 램핑하며 실측한 결과, 락이 없을 때는 매 구간에서 15~43건씩 중복이 생성됐고, 락을 연결한 뒤에는 1000명이 동시에 요청해도 예외 없이 정확히 1건만 성공했습니다. 구현된 안전장치가 실제 요청 경로에 연결돼 있는지까지 확인하는 게 왜 중요한지 체감한 사례입니다."
