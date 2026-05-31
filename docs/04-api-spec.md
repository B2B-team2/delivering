# 04 - API Spec

> 공통 규약, 도메인별 엔드포인트 목록, 요청/응답 예시, 에러 코드 요약

---

## 1. 공통 규약

### Base URL

```
http://{SERVER_URL}/api/v1
로컬: http://localhost:8080/api/v1
```

### 인증
- JWT 방식: `Authorization: Bearer {token}`
- 회원가입(`POST /auth/signup`), 로그인(`POST /auth/login`) 제외 전 API 인증 필수

### Content-Type
- `application/json`

### 페이지네이션
- `size`: 10 / 30 / 50 만 허용 (그 외 기본값 10)
- 기본 정렬: `sort=createdAt,DESC`

### Soft Delete
- 조회 응답에는 `deleted_at IS NULL` 데이터만 포함

---

## 2. 공통 응답 형식

### 성공 응답
```
{
  "status": 200,
  "message": "SUCCESS",
  "data": { }
}
```

### 에러 응답
```json
{
  "status": 400,
  "message": "VALIDATION_ERROR",
  "errors": [
    { "field": "email", "message": "이메일 형식이 올바르지 않습니다." }
  ]
}
```

### 페이지네이션 응답
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "sort": "createdAt,DESC"
  }
}
```

---

## 3. 도메인별 엔드포인트

> **Internal API**: `/api/v1/internal/` 경로는 서비스 간 FeignClient 전용. 외부 클라이언트 호출 불가.

### 3.1 User Service (`/api/v1`)

#### 인증 (Auth)

| Method | URL | 인증 필요 | 설명 |
|---|---|:---:|---|
| POST | `/auth/signup` | X | 회원가입 (PENDING 상태 생성) |
| POST | `/auth/login` | X | 로그인 + JWT 발급 (Keycloak) |
| POST | `/auth/logout` | O | 로그아웃 (`Authorization` + `Refresh-Token` 헤더) |
| POST | `/auth/refresh` | O | 토큰 재발급 (`Refresh-Token` 헤더) |

#### 사용자 관리 (Users)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/users/{userId}` | 본인, MASTER | 사용자 상세 조회 |
| PATCH | `/users/{userId}` | 본인, MASTER | 사용자 정보 수정 |
| DELETE | `/users/{userId}` | 본인, MASTER | 계정 삭제 (Soft Delete) |

#### 관리자 (Admin)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/admin/users` | MASTER | 전체 사용자 조회 (name, email, role 필터) |
| GET | `/admin/users/pending` | MASTER, HUB_MANAGER | 가입 대기 목록 조회 |
| PATCH | `/admin/users/{userId}/approval` | MASTER, HUB_MANAGER | 가입 승인/거절 |

#### 배송담당자 (Delivery Managers)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/delivery-managers` | MASTER, HUB_MANAGER | 배송담당자 목록 조회 (managerType, status, hubId 필터) |
| GET | `/delivery-managers/{userId}` | MASTER, HUB_MANAGER | 배송담당자 단건 조회 |
| POST | `/delivery-managers/assign` | MASTER, HUB_MANAGER | 배송담당자 수동 배정 |
| PATCH | `/delivery-managers/{userId}` | MASTER, HUB_MANAGER | 배송담당자 상태 변경 |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| POST | `/internal/users/manager-info` | 허브 기반 배송담당자 자동 배정 (fromHubId → 담당자 정보 반환) |

---

### 3.2 Company Service (`/api/v1`)

#### 업체 (Companies)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/companies` | MASTER, HUB_MANAGER | 신규 업체 등록 |
| GET | `/companies` | 전체 | 업체 목록 조회 |
| GET | `/companies/{companyId}` | 전체 | 업체 상세 조회 |
| PATCH | `/companies/{companyId}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 업체 정보 수정 |
| DELETE | `/companies/{companyId}` | MASTER, HUB_MANAGER | 업체 삭제 |

#### 배송지 (Delivery Addresses)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/companies/{companyId}/addresses` | COMPANY_MANAGER(소속) | 배송지 등록 |
| GET | `/companies/{companyId}/addresses` | COMPANY_MANAGER(소속) | 배송지 목록 조회 |
| PATCH | `/companies/addresses/{addressId}` | COMPANY_MANAGER(소속) | 배송지 수정 |
| DELETE | `/companies/addresses/{addressId}` | COMPANY_MANAGER(소속) | 배송지 삭제 |

#### 카테고리 (Categories)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/categories` | MASTER, HUB_MANAGER | 카테고리 등록 |
| GET | `/categories` | 전체 | 카테고리 목록 조회 |
| GET | `/categories/{categoryId}` | 전체 | 카테고리 상세 조회 |
| PATCH | `/categories/{categoryId}` | MASTER, HUB_MANAGER | 카테고리 수정 |
| DELETE | `/categories/{categoryId}` | MASTER, HUB_MANAGER | 카테고리 삭제 |

#### 상품 (Products)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/products` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 상품 등록 |
| GET | `/products` | 전체 | 상품 목록 조회 |
| GET | `/products/{productId}` | 전체 | 상품 상세 조회 |
| PATCH | `/products/{productId}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 정보 수정 |
| PATCH | `/products/{productId}/status` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 판매 상태 변경 |
| DELETE | `/products/{productId}` | MASTER, HUB_MANAGER | 상품 삭제 |

#### 상품 옵션 (Product Options)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/product-options` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 상품 옵션 생성 |
| GET | `/product-options` | 전체 | 상품 옵션 전체 조회 |
| GET | `/product-options/{productOptionId}` | 전체 | 상품 옵션 상세 조회 |
| PUT | `/product-options/{productOptionId}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 옵션 수정 |
| DELETE | `/product-options/{productOptionId}` | MASTER, HUB_MANAGER | 상품 옵션 삭제 |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| POST | `/internal/companies/hub-mapping` | 업체 ID 목록 → 허브 ID 매핑 일괄 조회 |
| GET | `/internal/companies/exists?hubId=` | 특정 허브에 업체 존재 여부 확인 |
| GET | `/internal/companies/{companyId}/default-address` | 업체 기본 배송지 조회 |
| POST | `/internal/product-options/details` | 상품 옵션 ID 목록 → 상세 정보 일괄 조회 |

---

### 3.3 Hub Service (`/api/v1`)

#### 허브 (Hubs)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/hubs` | MASTER | 신규 허브 등록 |
| GET | `/hubs` | 전체 | 전체 허브 조회 (hubType, status, keyword 필터) |
| GET | `/hubs/{hub_id}` | 전체 | 허브 상세 조회 |
| PATCH | `/hubs/{hub_id}` | MASTER | 허브 정보 수정 |
| DELETE | `/hubs/{hub_id}` | MASTER | 허브 삭제 |
| GET | `/hubs/{hub_id}/routes` | 전체 | 허브 연결 경로 목록 조회 |

#### 허브 연결 경로 (Hub Routes)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/hub-routes` | MASTER | 허브 연결 경로 생성 |
| GET | `/hub-routes` | 전체 | 전체 허브 경로 목록 조회 |
| GET | `/hub-routes/{route_id}` | 전체 | 허브 연결 경로 단건 조회 |
| PATCH | `/hub-routes/{route_id}` | MASTER | 허브 연결 경로 수정 |
| DELETE | `/hub-routes/{route_id}` | MASTER | 허브 연결 경로 삭제 |
| POST | `/hub-routes/search` | 전체 | 경유 포함 경로 탐색 (fromHubId → toHubId) |

#### 물류 창고 (Warehouses)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/warehouses` | MASTER, HUB_MANAGER(담당허브) | 물류 창고 등록 |
| GET | `/warehouses` | 전체 | 물류 창고 전체 조회 |
| GET | `/warehouses/{warehouse_id}` | 전체 | 물류 창고 단건 조회 |
| GET | `/warehouses/hub/{hub_id}` | 전체 | 허브 ID로 창고 조회 |
| PATCH | `/warehouses/{warehouse_id}` | MASTER, HUB_MANAGER(담당허브) | 물류 창고 수정 |
| DELETE | `/warehouses/{warehouse_id}` | MASTER, HUB_MANAGER(담당허브) | 물류 창고 삭제 |

#### 재고 (Inventory)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/inventory` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 재고 등록 |
| GET | `/inventory/{inventory_id}` | 전체 | 재고 단건 조회 |
| GET | `/inventory/warehouses/{warehouse_id}` | 전체 | 창고별 재고 목록 조회 |
| GET | `/inventory/{inventory_id}/histories` | 전체 | 재고 변동 이력 조회 (changeType, startDate, endDate 필터) |
| PATCH | `/inventory/{inventory_id}/adjust` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 재고 수동 조정 |
| DELETE | `/inventory/{inventory_id}` | MASTER, HUB_MANAGER | 재고 삭제 |
| PATCH | `/inventory/company-orders/{companyOrderId}/prepare` | MASTER, HUB_MANAGER | 출고 준비 (ORDERED → PREPARING) |
| PATCH | `/inventory/company-orders/{companyOrderId}/ship` | MASTER, HUB_MANAGER | 출고 완료 + 실재고 차감 (PREPARING → SHIPPED) |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| POST | `/internal/hub-routes/search` | 경유 포함 경로 탐색 (내부 전용) |
| POST | `/internal/inventory/reserve` | 주문 생성 시 재고 예약 |
| POST | `/internal/inventory/cancel` | 주문 취소 시 재고 예약 취소 (orderId 기준) |
| POST | `/internal/inventory/cancel/company` | 서브 주문 취소 시 재고 예약 취소 (companyOrderId 기준) |
| POST | `/internal/inventory/deduct` | 출고 시 실재고 차감 |
| POST | `/internal/inventory/return` | 반품 시 재고 복원 |

---

### 3.4 Order Service (`/api/v1`)

#### 임시주문 (Drafts)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/drafts` | COMPANY_MANAGER | 임시주문 항목 담기 |
| GET | `/drafts` | COMPANY_MANAGER(본인) | 임시주문 목록 조회 |
| PATCH | `/drafts/{draft_id}` | COMPANY_MANAGER(본인) | 임시주문 항목 수정 |
| DELETE | `/drafts/{draft_id}` | COMPANY_MANAGER(본인) | 임시주문 항목 삭제 |

#### 주문 (Orders)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/orders` | MASTER, COMPANY_MANAGER | 주문 생성|
| GET | `/orders` | MASTER, COMPANY_MANAGER(소속) | 주문 목록 조회 |
| GET | `/orders/{orderId}` | MASTER, COMPANY_MANAGER(관련) | 주문 상세 조회 |
| GET | `/orders/company/{companyOrderId}` | MASTER, COMPANY_MANAGER(관련) | 서브 주문 상세 조회 |
| PATCH | `/orders/{orderId}/cancel` | MASTER, COMPANY_MANAGER(수령업체) | 주문 전체 취소 |
| PATCH | `/orders/company/{companyOrderId}/cancel` | MASTER, COMPANY_MANAGER(관련) | 서브 주문 부분 취소 |

#### 결제 (Payments)

> 선결제 모델: 주문 생성 시 자동 COMPLETED 처리. 별도 결제 요청/승인 엔드포인트 없음.

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| PATCH | `/payments/{paymentId}/cancel` | MASTER, COMPANY_MANAGER(수령업체) | 결제 취소/환불 |
| GET | `/payments` | MASTER, COMPANY_MANAGER(수령업체) | 결제 목록 조회 |
| GET | `/payments/{paymentId}` | MASTER, COMPANY_MANAGER(수령업체) | 결제 단건 조회 |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| GET | `/internal/orders/company/{companyOrderId}/details` | 서브 주문 상세 정보 조회 (orderId + 상품 목록) |
| PATCH | `/internal/orders/company/{companyOrderId}/delivered` | 배송 완료 처리 (SHIPPED → DELIVERED, 전체 완료 시 Order → COMPLETED) |
| PATCH | `/internal/orders/company/{companyOrderId}/claim-cancel` | 클레임 취소 처리 |
| PATCH | `/internal/orders/company/{companyOrderId}/preparing` | 출고 준비 확인 (ORDERED → PREPARING) |
| PATCH | `/internal/orders/company/{companyOrderId}/shipped` | 출고 완료 (PREPARING → SHIPPED) |

---

### 3.5 Delivery Service (`/api/v1`)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/deliveries` | MASTER | 배송 목록 조회 |
| GET | `/deliveries/{delivery_id}` | 전체 | 배송 상세 조회 |
| GET | `/deliveries/tracking/{tracking_number}` | 전체 | 송장 번호로 배송 조회 |
| GET | `/delivery-addresses/{delivery_id}/{address_id}` | 전체 | 배송지 상세 조회 |
| PATCH | `/deliveries/{delivery_id}/status` | 전체 | 배송 상태 변경 |
| PATCH | `/deliveries/start/{trackingNumber}` | 전체 | 배송 출발 처리 (PENDING → SHIPPED) |
| PATCH | `/deliveries/complete/{trackingNumber}` | 전체 | 배송 완료 처리 (SHIPPED → DELIVERED) |
| PUT | `/deliveries/{delivery_id}/cancel` | 전체 | 배송 취소 |
| DELETE | `/deliveries/{deliveryId}` | MASTER, HUB_MANAGER, COMPANY_DELIVERY_MANAGER | 배송 삭제 (DELETED 상태) |
| GET | `/deliveries/{delivery_id}/routes` | 전체 | 세부 경로 조회 |
| PATCH | `/deliveries/{delivery_id}/routes/{route_id}` | 전체 | 경로 구간 상태 업데이트 |
| DELETE | `/deliveries/{delivery_id}/routes/{route_id}` | 전체 | 경로 구간 삭제 |
| GET | `/deliveries/{delivery_id}/logs` | 전체 | 배송 이벤트 로그 조회 |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| POST | `/internal/deliveries` | 배송 + 경로 일괄 생성 (주문 생성 시 호출) |
| POST | `/internal/deliveries/cancel` | 주문 취소 시 배송 취소 |

---

### 3.6 Operations Service (`/api/v1`)

#### 클레임 (Claims)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/claims` | COMPANY_MANAGER | 반품/교환 접수 |
| GET | `/claims` | MASTER, HUB_MANAGER | 클레임 목록 조회 |
| GET | `/claims/{claimId}` | MASTER, HUB_MANAGER | 클레임 상세 조회 |
| PATCH | `/claims/{claimId}/status` | MASTER, HUB_MANAGER | 클레임 상태 업데이트 |

#### 슬랙 알림 (Slack)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/slack/send` | MASTER, HUB_MANAGER, HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER | 슬랙 메시지 발송 |
| GET | `/slack/messages` | MASTER | 발송 이력 목록 조회 |
| GET | `/slack/send/{message_id}` | MASTER | 발송 이력 상세 조회 |

#### AI (AI Requests)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/ai/generate` | 전체 | 발송 시한 예측 (Gemini API) |
| GET | `/requests` | 전체 | AI 요청 로그 전체 조회 |
| GET | `/requests/{request_id}` | 전체 | AI 요청 로그 상세 조회 |

#### Internal (FeignClient 전용)

| Method | URL | 설명 |
|---|---|---|
| GET | `/internal/claims/{claimId}` | 클레임 상세 조회 (내부 서비스 전용) |

---

## 4. 요청/응답 예시

### 회원가입

**Request**
```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123!",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "slack_id": "U12345678"
}
```

**Response**
```json
{
  "status": 201,
  "message": "SUCCESS",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "approvalStatus": "PENDING"
  }
}
```

### 주문 생성

**Request**
```http
POST /api/v1/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "receiverCompanyId": "...",
  "recipientName": "김말숙",
  "phone": "010-9876-5432",
  "addressId": "...",
  "dueDate": "2026-05-29T15:00:00",
  "requestMemo": "5월 29일 3시까지 납품 부탁드립니다",
  "items": [
    {
      "productOptionId": "...",
      "quantity": 50
    }
  ]
}
```

**Response**
```json
{
  "status": 201,
  "message": "SUCCESS",
  "data": {
    "orderId": "...",
    "status": "PENDING",
    "totalPrice": 500000,
    "deliveryFee": 5000,
    "finalPrice": 505000
  }
}
```

---

## 5. 공통 에러 코드

> 모든 서비스에서 공통으로 사용하는 에러 (`CommonErrorCode`). 서비스별 도메인 에러는 각 서비스 내부 `ErrorCode` enum 참조.

| HTTP 상태 | 코드 | 에러명 | 설명 |
|---|---|---|---|
| 400 | `C002` | `INVALID_PARAMETER_TYPE` | 잘못된 파라미터 타입 |
| 400 | `C003` | `MISSING_PATH_VARIABLE` | 필수 경로 변수 누락 |
| 401 | - | `UNAUTHORIZED` | 인증 토큰 없음 또는 만료 (Gateway / Keycloak) |
| 403 | `C001` | `ACCESS_DENIED` | 해당 작업을 수행할 권한이 없습니다 |
| 405 | `C004` | `METHOD_NOT_ALLOWED` | 지원하지 않는 HTTP 메서드 |
| 500 | `C005` | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |
