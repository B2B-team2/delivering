# 02 - Domain Spec

> 도메인 목록, 도메인별 상태/흐름, 권한 체계, 도메인 관계 요약

---

## 1. 도메인 목록

| 도메인 | 서비스 | 설명 |
|---|---|---|
| User | user-service | 회원가입(승인 기반), 로그인, 권한 관리, 배송담당자 프로필 |
| Company | company-service | 생산/수령업체 등록·관리, 상품·옵션 관리 |
| Hub | hub-service | 전국 17개 허브 센터, 허브 간 이동 경로, 창고 및 재고 관리 |
| Order | order-service | 업체 간 상품 주문 생성·관리, 임시 주문, 결제 |
| Delivery | delivery-service | 배송 전체 상태·경로 기록 관리, 배송 이벤트 로그 |
| Operations | operations-service | 클레임, 슬랙 알림, AI 발송 시한 요청 로그 |

---

## 2. 도메인별 상태 흐름

### 2.1 회원가입 승인 흐름 (User)

```
PENDING (가입 요청)
  ├─ APPROVED (승인 완료 → 로그인 가능)
  └─ REJECTED (거절 → 로그인 불가, rejected_reason 저장)
```

### 2.2 주문 상태 흐름 (Order — p_orders)

```
PENDING (주문 대기)
  ├─ DELIVERING (배송 중)
  │    └─ COMPLETED (배송 완료)
  └─ CANCELLED (주문 취소)
```

### 2.3 업체별 주문 상태 흐름 (Order — p_company_orders)

```
ORDERED (주문 접수)
  └─ PREPARING (출고 준비 중)
       └─ SHIPPED (상품 출고)
            └─ DELIVERED (수령업체 도착)
CANCELLED (주문 취소, 어느 단계에서나 가능)
```

### 2.4 배송 상태 흐름 (Delivery — p_deliveries)

```
PENDING (배송 대기)
  └─ PREPARING (출고 준비 중)
       └─ SHIPPED (배송 출발)
            └─ DELIVERED (배송 완료)
CANCELLED (배송 취소)
DELETED (관리자 삭제)
```

### 2.5 배송 경로 상태 흐름 (Delivery — p_delivery_routes)

```
PENDING (대기 — 출발 전 구간)
  └─ MOVING (이동 중 — 현재 이동 중인 구간)
       └─ ARRIVED (도착 완료)
CANCELLED (경로 취소)
```

### 2.6 결제 상태 흐름 (Order — p_payments)

```
PENDING (결제 대기)
  ├─ COMPLETED (결제 완료)
  └─ CANCELLED (결제 취소)
```

### 2.7 클레임 상태 흐름 (Operations — p_order_claims)

```
REQUESTED (접수)
  ├─ PROCESSING (처리 중)
  │    ├─ COMPLETED (처리 완료 + refund_amount 확정)
  │    └─ REJECTED (반려)
  └─ CANCELLED (취소)
```

### 2.8 슬랙 메시지 발송 상태 (Operations — p_slack_messages)

```
PENDING (발송 전)
  ├─ SENT (발송 완료)
  └─ FAILED (발송 실패 → 재시도)
```

### 2.9 배송담당자 근무 상태 (User — p_delivery_managers)

```
WAITING (대기) → DELIVERING (배송 중) → INACTIVE (근무 중지)
```

---

## 3. 권한 체계

### 3.1 역할 정의

| 역할 코드 | 설명 | 비고 |
|---|---|---|
| `MASTER` | 모든 기능 전체 권한 | p_admins.role = MASTER |
| `HUB_MANAGER` | 담당 허브의 배송담당자·업체·상품 관리 | p_hub_managers |
| `HUB_DELIVERY_MANAGER` | 허브 간 배송 전담 | p_delivery_managers.manager_type = HUB_DELIVERY |
| `COMPANY_DELIVERY_MANAGER` | 최종 허브 → 수령업체 배송 전담 | p_delivery_managers.manager_type = COMPANY_DELIVERY |
| `COMPANY_MANAGER` | 소속 업체 정보 및 상품 관리 | p_company_managers |

### 3.2 역할별 주요 권한 매트릭스

| 기능 | MASTER | HUB_MANAGER | HUB_DELIVERY | COMPANY_DELIVERY | COMPANY_MANAGER |
|---|:---:|:---:|:---:|:---:|:---:|
| 가입 승인/거절 | O | O | - | - | - |
| 허브 CRUD | O | - | - | - | - |
| 업체 등록/수정 | O | O(담당허브) | - | - | O(소속) |
| 상품 CRUD | O | O | - | - | O(소속) |
| 재고 조정 | O | O(담당허브) | - | - | - |
| 주문 조회 (전체) | O | O | - | - | - |
| 주문 생성 | O | - | - | - | O |
| 배송 상태 변경 | O | - | O(담당구간) | O(담당구간) | - |
| 담당자 재배정 | O | O | - | - | - |
| 클레임 처리 | O | O | - | - | - |
| 클레임 접수 | - | - | - | - | O |

### 3.3 배송담당자 구간 제한

| 담당자 유형 | 담당 구간 | 불가 구간 |
|---|---|---|
| HUB_DELIVERY_MANAGER | 허브 → 허브 | 최종 허브 → 수령업체 |
| COMPANY_DELIVERY_MANAGER | 최종 허브 → 수령업체 | 허브 간 이동 |

---

## 4. 도메인 관계 요약

### 4.1 서비스 간 FeignClient 호출 관계

```
Order Service
  ├─→ Company Service   : Hub ID 조회 (fromHubId / toHubId 도출)
  ├─→ Hub Service       : 재고 예약/취소/차감
  ├─→ Delivery Service  : 배송 생성
  └─→ Operations Service: AI 발송 시한 계산 요청

Delivery Service
  ├─→ Hub Service       : 허브 경로 탐색
  ├─→ User Service      : 배송담당자 순번 배정
  └─→ Order Service     : 배송 완료(DELIVERED) 시 company_order 상태 갱신

Operations Service
  ├─→ Hub Service       : 클레임 반품 승인 시 재고 복원
  └─→ Order Service     : 클레임 반품 승인 시 주문 CANCELLED 처리
```

---

### 4.2 서비스별 내부 엔티티 관계

> DB가 서비스별로 물리적으로 분리되어 있으므로 서비스 경계를 넘는 참조는 UUID만 존재 (실제 FK 없음).
> 아래는 서비스 내부의 물리 FK 관계만 표기한다.

```
[user-service]
p_users ──1:1── p_admins
p_users ──1:1── p_hub_managers
p_users ──1:1── p_delivery_managers
p_users ──1:1── p_company_managers

[company-service]
p_product_categories ──1:N── p_products ──1:N── p_product_options
p_companies ──1:N── p_products
p_companies ──1:N── p_delivery_addresses

[hub-service]
p_logistics_hubs ──1:N── p_hub_routes (from_hub_id / to_hub_id)
p_logistics_hubs ──1:1── p_warehouses ──1:N── p_warehouse_inventory ──1:N── p_inventory_histories

[order-service]
p_orders ──1:N── p_company_orders ──1:N── p_order_items
p_orders ──1:1── p_payments

[delivery-service]
p_deliveries ──1:N── p_delivery_routes
p_deliveries ──1:N── p_delivery_log

[operations-service]
p_order_claims  (company_order_id: UUID 참조만)
p_slack_messages (receiver_user_id: UUID 참조만)
p_ai_requests   (delivery_id: UUID 참조만)
```

---

### 4.3 스키마 분리

| 스키마 | 소속 테이블 |
|---|---|
| USER | p_users, p_admins, p_hub_managers, p_delivery_managers, p_company_managers |
| COMPANY | p_companies, p_product_categories, p_products, p_product_options, p_delivery_addresses |
| HUB | p_logistics_hubs, p_hub_routes, p_warehouses, p_warehouse_inventory, p_inventory_histories |
| ORDER | p_orders, p_company_orders, p_order_items, p_order_drafts, p_payments |
| DELIVERY | p_deliveries, p_delivery_routes, p_delivery_log |
| OPS | p_order_claims, p_slack_messages, p_ai_requests |
