# 05 - Service Spec

> 서비스 로직 명세 및 주요 유스케이스 핵심 흐름

---

## 1. 회원가입 승인 프로세스

**관여 서비스**: User Service

```
1. 가입 요청
   - POST /auth/signup 호출
   - p_users 저장 (approval_status = PENDING)

2. 관리자 검토
   - MASTER 또는 HUB_MANAGER가 GET /admin/users/pending 으로 대기 목록 확인

3. 승인 또는 거절
   - PATCH /admin/users/{user_id}/approval
   - 승인 → approval_status = APPROVED, approved_by, approved_at 저장
   - 거절 → approval_status = REJECTED, rejected_reason 저장

4. 로그인 가능 여부
   - APPROVED 상태일 때만 POST /auth/login 허용
```

**핵심 규칙**
- REJECTED 상태 사용자는 로그인 불가
- 승인 처리자는 `approved_by`에 관리자 UUID 저장

---

## 2. 주문 및 출고 프로세스

**관여 서비스**: Order Service → Hub Service → Delivery Service → Operations Service

```
1. 임시 주문
   - COMPANY_MANAGER가 POST /drafts 로 품목 담기
   - p_order_drafts에 저장

2. 주문 생성
   - POST /orders 호출
   - p_orders 생성 (status = PENDING)
   - 업체별 p_company_orders 분할 생성 (status = ORDERED)

3. 재고 예약 [Hub Service FeignClient 호출]
   - 상품 옵션(SKU)별 quantity - reserved_quantity 검증
   - 가용 재고 충분 → reserved_quantity 증가
   - p_inventory_histories 기록 (change_type = RESERVED)
   - 재고 부족 → 주문 실패, 전체 롤백

4. 결제 처리
   - POST /payments/ready → POST /payments/confirm
   - p_payments.status = COMPLETED

5. 출고 준비 (허브 관리자 확인)
   - HUB_MANAGER가 p_company_orders.status → PREPARING

6. 배송 생성 [Delivery Service FeignClient 호출]
   - POST /deliveries 내부 호출
   - p_deliveries 생성 + 전체 경로(p_delivery_routes) 일괄 생성
   - p_company_orders.status → SHIPPED

7. AI 발송 시한 계산 [Operations Service FeignClient 호출]
   - POST /ai/predict-deadline 내부 호출
   - Gemini API로 final_deadline_at 계산
   - p_ai_requests에 요청/응답 전문 저장

8. 슬랙 알림 발송
   - POST /slack/send 내부 호출
   - 허브 담당자에게 발송 시한 포함 메시지 전송
   - p_slack_messages 이력 저장
```

### Saga 구현의 한계 및 향후 과제

현재 Saga 보상 스택은 JVM 메모리(`ArrayDeque`)에만 존재합니다.
재고 예약 성공 직후 `order-service`가 비정상 종료되면 보상 스택이 유실되어
예약된 재고를 롤백할 방법이 없습니다.

**현재 한계**
- Saga 진행 상태가 DB에 영속화되지 않음
- 보상 트랜잭션 실패 시 수동 복구 로그만 남김
- 서비스 재시작 후 미완료 Saga를 재개할 수단 없음

**향후 개선 방향**
- Outbox 패턴 도입: DB 변경과 이벤트 발행을 하나의 트랜잭션으로 묶어 메시지 유실 방지
- Saga 상태 영속화: 각 단계별 진행 상태를 DB에 기록하여 장애 복구 가능하도록 개선
- Kafka 기반 비동기 전환: 현재 동기 FeignClient 체인을 이벤트 기반으로 전환하여 서비스 간 결합도 감소
- 멱등성 키 적용: 재고 예약·배송 생성 호출 시 중복 처리 방지

---

## 3. 재고 처리 프로세스

**관여 서비스**: Order Service → Hub Service

### 주문 생성 시
```
1. 재고 확인
   - 가용 재고 = quantity - reserved_quantity
   - 주문 수량 >= 가용 재고 → 실패 반환

2. 재고 예약
   - reserved_quantity += 주문 수량
   - p_inventory_histories (change_type = RESERVED)
   - 낙관적 락(version) 적용 → 충돌 시 재시도
```

### 주문 취소 시
```
1. 예약 취소 [Hub Service FeignClient 호출]
   - reserved_quantity -= 취소 수량
   - p_inventory_histories (change_type = CANCELLED)
```

### 출고 처리 시
```
1. 재고 차감 [Hub Service FeignClient 호출]
   - quantity -= 출고 수량
   - reserved_quantity -= 출고 수량
   - p_inventory_histories (change_type = OUTBOUND)
```

**동시성 제어**: 낙관적 락(`version` 컬럼) — 충돌 발생 시 `OPTIMISTIC_LOCK_FAILURE` 에러 반환 후 재시도

---

## 4. 배송 및 경로 관리 프로세스

**관여 서비스**: Delivery Service → Hub Service → User Service → Order Service

```
1. 경로 생성
   - Hub Service의 p_hub_routes 조합으로 전체 구간 탐색
   - POST /hub-routes/search 호출 (경유 포함)
   - p_delivery_routes 일괄 생성 (sequence 순번 부여)

2. 배송담당자 자동 배정 [User Service FeignClient 호출]
   - delivery_order(순번) 기준으로 자동 배정
   - 허브 간 구간 → HUB_DELIVERY_MANAGER 배정
   - 최종 허브→수령업체 구간 → COMPANY_DELIVERY_MANAGER 배정
   - p_delivery_log에 MANAGER_ASSIGNED 이벤트 INSERT

3. 배송 수행
   - 배송매니저가 PATCH /deliveries/{id}/routes/{route_id} 로 구간 상태 업데이트
   - PENDING → MOVING → ARRIVED
   - 상태/담당자 변경 시 p_delivery_log에 이벤트 append
   - 이동 완료 후 actual_distance, actual_duration 업데이트

4. 완료 처리
   - 마지막 구간 ARRIVED 시
     - p_deliveries.status → COMPLETED, completed_at 기록
     - Order Service FeignClient 호출로 p_orders.status → COMPLETED
     - p_company_orders.status → DELIVERED
```

**담당자 재배정 시**
- PATCH /deliveries/{id}/manager 호출
- p_delivery_log에 MANAGER_CHANGED 이벤트 자동 INSERT
  ```json
  {
    "event_type": "MANAGER_CHANGED",
    "previous_value": {"manager": "김철수"},
    "current_value":  {"manager": "박영희"},
    "reason": "담당자 건강 문제로 긴급 교체"
  }
  ```

---

## 5. 클레임 처리 프로세스 (반품/교환)

**관여 서비스**: Operations Service → Hub Service → Order Service

```
1. 클레임 접수
   - POST /claims (COMPANY_MANAGER)
   - claim_type: RETURN / EXCHANGE
   - p_order_claims.status = REQUESTED

2. 클레임 검토
   - HUB_MANAGER 또는 MASTER 검토
   - 처리 시작 → status = PROCESSING
   - 반려   → status = REJECTED (처리 종료)

3. 반품 승인 시 재고 복원 [Hub Service FeignClient 호출]
   - quantity += 반품 수량
   - p_inventory_histories (change_type = RETURNED)

4. 주문 상태 변경 [Order Service FeignClient 호출]
   - p_orders.status → CANCELLED
   - p_company_orders.status → CANCELLED

5. 클레임 완료
   - p_order_claims.status = COMPLETED
   - refund_amount 확정 업데이트
```

---

## 6. AI 발송 시한 계산

**관여 서비스**: Order Service → Operations Service → Gemini API → Slack

```
1. 계산 요청
   - 주문 생성 완료 후 Order Service가 Operations Service로 FeignClient 호출
   - POST /ai/predict-deadline

2. 프롬프트 구성 (Operations Service 내부)
   - 상품명 및 수량
   - 납기 일시 (due_date)
   - 발송지(departure_hub), 경유지, 도착지(destination_hub)
   - 배송담당자 근무시간 (09:00 ~ 18:00)

3. AI 호출
   - Operations Service → Gemini API (REST)
   - 요청/응답 전문을 p_ai_requests에 저장
   - AI 응답에서 final_deadline_at 파싱 후 구조화 저장
   - p_deliveries.final_dispatch_deadline_at 업데이트

4. 슬랙 발송
   - Operations Service → Slack Incoming Webhook
   - 허브 담당자에게 최종 발송 시한 포함 메시지 전송
   - p_slack_messages에 발송 이력 저장
```

**슬랙 발송 메시지 예시**
```
주문 번호: 1
주문자 정보: 김말숙 / msk@seafood.world
주문 시간: 2026-05-20 10:00:00
상품 정보: 마른 오징어 50박스
요청 사항: 5월 29일 3시까지 납품 부탁드립니다
발송지: 경기 북부 센터
경유지: 경기남부 센터, 대구광역시 센터
도착지: 부산시 사하구 낙동대로 1번길 1 해산물월드
배송담당자: 고길동 / kdk@sparta.world

위 내용을 기반으로 도출된 최종 발송 시한은 5월 27일 오전 9시입니다.
```

---

## 7. 서비스별 핵심 규칙 요약

### User Service
- 승인 기반 가입: PENDING → APPROVED 상태여야 로그인 가능
- 배송담당자 자동 배정: `delivery_order` 순번 기준 Round-Robin
  - 배정 순서: 순번 0 → 1 → ... → N → 0 (순환)
  - 신규 배송담당자 추가 시 가장 마지막 순번으로 설정
  - **삭제된 담당자의 순번은 재배열하지 않음** (빈 순번 그대로 유지)
- 배송담당자 담당 구간 제한 (HUB vs COMPANY 타입)

### Hub Service
- 허브·경로 정보는 변경이 적으므로 Redis **Cache-Aside** 전략 적용
- 재고 예약/차감 시 **낙관적 락** 필수 (`@Version` 어노테이션)
- 재고 변동 이력은 append-only (수정/삭제 불가)

### Order Service
- 주문 생성은 재고 예약 성공 후에만 확정
- 주문 취소 시 재고 복원 + 결제 취소 연동 필요
- 임시 주문(drafts)은 실제 재고와 무관하게 저장

### Delivery Service
- 배송 취소는 **PENDING 상태에서만** 허용
- 모든 상태/담당자 변경은 `p_delivery_log`에 append 기록
- 배송 경로는 주문 시 전체 일괄 생성, 이후 actual 값만 업데이트

### Operations Service
- 슬랙 발송 실패 시 PENDING → FAILED, 재시도 로직 적용
- AI 요청 로그(`p_ai_requests`)는 요청/응답 전문 보존
- 클레임 처리 시 재고 복원 + 주문 취소 원자성 보장 (FeignClient + 롤백)
