# 배송 중복 생성 검증 결과

## 배경

동일한 `companyOrderId`로 동시에 여러 건의 배송 생성 요청이 들어올 때 중복 생성을 막는 장치가 실제로 동작하는지 검증했다.

## 원인 분석

- `DeliveryLockFacade`(Redisson 분산 락, `lock:order:{companyOrderId}`)가 이미 구현돼 있었지만, 실제 요청 경로인 `DeliveryInternalController` → `DeliveryService.createDelivery()`가 이 락을 **거치지 않고** 호출되고 있었다(완전히 미사용 상태의 죽은 코드).
- 유일한 중복 방지 장치는 `DeliveryService.createSingleDelivery()` 안의 `deliveryRepository.existsByCompanyOrderId(...)` 체크뿐이었다.
- `Delivery` 엔티티의 `company_order_id` 컬럼에는 DB 유니크 제약도 없어, 락도 DB 제약도 없는 상태로 check-then-act 경쟁 조건에 그대로 노출돼 있었다.

## 1차 검증 — 락 미적용 상태에서 중복 발생 재현

동일 `companyOrderId`로 20개 스레드가 동시에 `DeliveryService.createSingleDelivery()`를 직접 호출(컨트롤러가 실제로 하던 것과 동일한 경로)하도록 테스트를 작성했다. DB 존재 체크는 실제 DB 왕복 지연을 흉내낸 30ms 지연을 준 Fake로 대체해 경쟁 조건을 안정적으로 재현했다.

**결과: 20건 요청 중 20건 모두 중복 생성 (0건만 정상 거부)**

## 수정

`DeliveryInternalController`가 `DeliveryService` 대신 이미 구현돼 있던 `DeliveryLockFacade.createDeliveriesWithLock()`을 호출하도록 변경(1줄 의존성 교체 + 메서드 호출 변경).

```diff
- List<DeliveryCreateResponse> response = deliveryService.createDelivery(requests);
+ List<DeliveryCreateResponse> response = deliveryLockFacade.createDeliveriesWithLock(requests);
```

## 2차 검증 — 락 적용 후 재실행

같은 20건 동시 요청 시나리오를 실제 Redis(로컬 6379)에 연결된 Redisson 락으로 재실행.

**결과: 20건 요청 중 1건만 생성 성공, 19건 정상 거부 (락 경합 실패 또는 DB 중복 체크로 차단)**

## 결론 (면접 답변용)

> "배송 생성 시 중복 방지를 위해 Redisson 분산 락(`DeliveryLockFacade`)이 이미 구현돼 있었지만, 실제 컨트롤러가 이 락을 거치지 않고 서비스 메서드를 직접 호출하고 있다는 걸 코드 추적으로 발견했습니다. 동시성 테스트로 실측한 결과, 락 없이 20건을 동시 요청하면 20건 모두 중복 생성됐습니다. 컨트롤러가 락 파사드를 거치도록 한 줄을 고치고 재검증하니 20건 중 1건만 정상 생성되고 나머지는 올바르게 거부됐습니다. 구현된 안전장치가 실제 요청 경로에 연결돼 있는지까지 확인하는 게 왜 중요한지 체감한 사례입니다."
