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
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": { ... }
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
    "content": [...],
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

### 3.1 User Service (`/api/v1`)

#### 인증 (Auth)

| Method | URL | 인증 필요 | 설명 |
|---|---|:---:|---|
| POST | `/auth/signup` | X | 회원가입 (PENDING 상태 생성) |
| POST | `/auth/login` | X | 로그인 + JWT 발급 |
| POST | `/auth/logout` | O | 로그아웃 |
| POST | `/auth/refresh` | O | 토큰 재발급 |

#### 사용자 관리 (Users)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/users/{user_id}` | 본인, MASTER | 사용자 상세 조회 |
| PATCH | `/users/{user_id}` | 본인, MASTER | 사용자 정보 수정 |
| DELETE | `/users/{user_id}` | 본인, MASTER | 계정 삭제 (Soft Delete) |

#### 관리자 (Admin)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/admin/users` | MASTER | 전체 사용자 조회 |
| GET | `/admin/users/pending` | MASTER, HUB_MANAGER | 가입 대기 목록 조회 |
| PATCH | `/admin/users/{user_id}/approval` | MASTER, HUB_MANAGER | 가입 승인/거절 |

#### 배송담당자 (Delivery Managers)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/delivery-managers` | MASTER, HUB_MANAGER | 배송담당자 목록 조회 |
| GET | `/delivery-managers/{delivery_manager_id}` | MASTER, HUB_MANAGER | 배송담당자 단건 조회 |
| PATCH | `/delivery-managers/{delivery_manager_id}/status` | MASTER, HUB_MANAGER | 배송담당자 상태 변경 |
| POST | `/delivery-managers/assign` | 시스템 내부 | 순번 기반 자동 배정 |

---

### 3.2 Company Service (`/api/v1`)

#### 업체 (Companies)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/companies` | MASTER, HUB_MANAGER | 신규 업체 등록 |
| GET | `/companies` | 인증 사용자 | 업체 목록 조회 |
| GET | `/companies/{company_id}` | 인증 사용자 | 업체 상세 조회 |
| PATCH | `/companies/{company_id}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 업체 정보 수정 |
| DELETE | `/companies/{company_id}` | MASTER, HUB_MANAGER | 업체 삭제 |

#### 배송지 (Delivery Addresses)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/companies/{company_id}/addresses` | COMPANY_MANAGER | 배송지 등록 |
| GET | `/companies/{company_id}/addresses` | 인증 사용자 | 배송지 목록 조회 |
| PATCH | `/companies/addresses/{address_id}` | COMPANY_MANAGER(소속) | 배송지 수정 |
| DELETE | `/companies/addresses/{address_id}` | COMPANY_MANAGER(소속) | 배송지 삭제 |

#### 카테고리 (Categories)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/categories` | MASTER, HUB_MANAGER | 카테고리 등록 |
| GET | `/categories` | 인증 사용자 | 카테고리 조회 |
| GET | `/categories/{category_id}` | 인증 사용자 | 카테고리 상세 조회 |
| PATCH | `/categories/{category_id}` | MASTER, HUB_MANAGER | 카테고리 수정 |
| DELETE | `/categories/{category_id}` | MASTER, HUB_MANAGER | 카테고리 삭제 |

#### 상품 (Products)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/products` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 상품 등록 |
| GET | `/products` | 인증 사용자 | 상품 목록 조회 |
| GET | `/products/{product_id}` | 인증 사용자 | 상품 상세 조회 |
| PATCH | `/products/{product_id}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 정보 수정 |
| PATCH | `/products/{product_id}/status` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 판매 상태 변경 |
| DELETE | `/products/{product_id}` | MASTER, HUB_MANAGER | 상품 삭제 |

#### 상품 옵션 (Product Options)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/product-options` | MASTER, HUB_MANAGER, COMPANY_MANAGER | 상품 옵션 생성 |
| GET | `/product-options` | 인증 사용자 | 상품 옵션 전체 조회 |
| GET | `/product-options/{product_option_id}` | 인증 사용자 | 상품 옵션 상세 조회 |
| PUT | `/product-options/{product_option_id}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 옵션 수정 |
| PATCH | `/products/{product_id}/options/{option_id}` | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 옵션 개별 수정 |
| DELETE | `/product-options/{product_option_id}` | MASTER, HUB_MANAGER | 상품 옵션 삭제 |

---

### 3.3 Hub Service (`/api/v1`)

#### 허브 (Hubs)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/hubs` | MASTER | 신규 허브 등록 |
| GET | `/hubs` | 인증 사용자 | 전체 허브 조회 (Redis 캐싱) |
| GET | `/hubs/{hub_id}` | 인증 사용자 | 허브 상세 조회 |
| PATCH | `/hubs/{hub_id}` | MASTER | 허브 정보 수정 |
| DELETE | `/hubs/{hub_id}` | MASTER | 허브 삭제 |

#### 허브 연결 경로 (Hub Routes)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/hub-routes` | MASTER | 허브 연결 경로 생성 |
| GET | `/hubs/{hub_id}/routes` | 인증 사용자 | 허브 연결 경로 조회 |
| GET | `/hub-routes/{route_id}` | 인증 사용자 | 허브 연결 경로 단건 조회 |
| PATCH | `/hub-routes/{route_id}` | MASTER | 허브 연결 경로 수정 |
| DELETE | `/hub-routes/{route_id}` | MASTER | 허브 연결 경로 삭제 |
| POST | `/hub-routes/search` | 시스템 내부 | 경유 포함 경로 탐색 |

> `POST /hub-routes/search`: 단순 1:1이 아닌 경유 허브를 포함한 경로 탐색이므로 POST 사용

#### 물류 창고 (Warehouses)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/warehouses` | MASTER, HUB_MANAGER | 물류 창고 등록 |
| GET | `/warehouses` | MASTER, HUB_MANAGER | 물류 창고 조회 |
| GET | `/warehouses/{warehouse_id}` | MASTER, HUB_MANAGER | 물류 창고 단건 조회 |
| PATCH | `/warehouses/{warehouse_id}` | MASTER, HUB_MANAGER | 물류 창고 수정 |
| DELETE | `/warehouses/{warehouse_id}` | MASTER | 물류 창고 삭제 |
| GET | `/warehouses/{warehouse_id}/inventory` | MASTER, HUB_MANAGER | 창고 재고 조회 |

#### 재고 (Inventory)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/inventory` | MASTER, HUB_MANAGER | 재고 등록 |
| GET | `/inventory/{inventory_id}/histories` | MASTER, HUB_MANAGER | 재고 변동 이력 조회 |
| POST | `/inventory/{inventory_id}/adjust` | MASTER, HUB_MANAGER | 재고 수동 조정 |
| DELETE | `/inventory/{inventory_id}` | MASTER | 재고 삭제 |

---

### 3.4 Order Service (`/api/v1`)

#### 장바구니 (Drafts)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/drafts` | COMPANY_MANAGER | 장바구니 담기 |
| GET | `/drafts` | COMPANY_MANAGER(본인) | 장바구니 조회 |
| PATCH | `/drafts/{draft_id}` | COMPANY_MANAGER(본인) | 장바구니 수정 |
| DELETE | `/drafts/{draft_id}` | COMPANY_MANAGER(본인) | 장바구니 삭제 |

#### 주문 (Orders)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/orders` | COMPANY_MANAGER | 주문 생성 |
| GET | `/orders` | MASTER, HUB_MANAGER | 전체 주문 조회 |
| GET | `/orders/{order_id}` | 관련 업체 담당자, MASTER | 주문 상세 조회 |
| GET | `/orders/company/{company_order_id}` | 관련 업체 담당자, MASTER | 서브 주문 상세 조회 |
| PATCH | `/orders/{order_id}/cancel` | COMPANY_MANAGER(요청자), MASTER | 주문 전체 취소 |
| PATCH | `/orders/company/{company_order_id}/cancel` | COMPANY_MANAGER(요청자), MASTER | 서브 주문 부분 취소 |

#### 결제 (Payments)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/payments/ready` | COMPANY_MANAGER | 결제 요청 (PG 준비) |
| POST | `/payments/confirm` | COMPANY_MANAGER | 결제 승인 및 검증 |
| POST | `/payments/{payment_id}/cancel` | MASTER, COMPANY_MANAGER | 결제 취소/환불 |
| GET | `/payments` | 관련 사용자, MASTER | 결제 내역 조회 |
| GET | `/payments/{payment_id}` | 관련 사용자, MASTER | 결제 단건 조회 |

---

### 3.5 Delivery Service (`/api/v1`)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/deliveries` | 시스템 내부 | 배송 생성 (주문 생성 시 자동) |
| GET | `/deliveries` | 관련 담당자, MASTER | 배송 목록 조회 |
| GET | `/deliveries/{delivery_id}` | 관련 담당자, MASTER | 배송 상세 조회 |
| GET | `/deliveries/tracking/{tracking_number}` | 인증 사용자 | 송장 번호로 배송 조회 |
| PATCH | `/deliveries/{delivery_id}/status` | 담당 배송매니저, MASTER | 배송 상태 변경 |
| POST | `/deliveries/{delivery_id}/cancel` | MASTER, HUB_MANAGER | 배송 취소 (PENDING만 허용) |
| PATCH | `/deliveries/{delivery_id}/manager` | MASTER, HUB_MANAGER | 담당자 재배정 |
| GET | `/deliveries/{delivery_id}/routes` | 관련 담당자, MASTER | 세부 경로 조회 |
| PATCH | `/deliveries/{delivery_id}/routes/{route_id}` | 담당 배송매니저 | 경로 구간 상태 업데이트 |
| GET | `/deliveries/{delivery_id}/logs` | 관련 담당자, MASTER | 배송 상태 이력 조회 |
| GET | `/delivery-addresses/{address_id}` | 인증 사용자 | 배송지 상세 조회 |

---

### 3.6 Operations Service (`/api/v1`)

#### 클레임 (Claims)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/claims` | COMPANY_MANAGER | 반품/교환 접수 |
| GET | `/claims` | MASTER, HUB_MANAGER | 클레임 목록 조회 |
| GET | `/claims/{claim_id}` | MASTER, HUB_MANAGER | 클레임 상세 조회 |
| PATCH | `/claims/{claim_id}/status` | MASTER, HUB_MANAGER | 클레임 상태 업데이트 |

#### 슬랙 알림 (Slack)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/slack/send` | 시스템 내부 | 메시지 발송 요청 |
| GET | `/slack/messages` | MASTER, HUB_MANAGER | 메시지 이력 조회 |
| GET | `/slack/send/{message_id}` | MASTER, HUB_MANAGER | 메시지 상세 이력 조회 |

#### AI (AI Requests)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/ai/predict-deadline` | 시스템 내부 | 발송 시한 예측 (Gemini API) |
| POST | `/ai/requests` | 시스템 내부 | AI 요청 로그 생성 |
| GET | `/ai/requests` | MASTER | AI 요청 로그 조회 |
| GET | `/ai/requests/{request_id}` | MASTER | AI 요청 로그 상세 조회 |

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

## 5. 에러 코드 요약

| HTTP 상태 | 코드 | 설명 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 요청 값 형식 오류 |
| 400 | `INVALID_STATUS` | 유효하지 않은 상태 전환 |
| 401 | `UNAUTHORIZED` | 인증 토큰 없음 또는 만료 |
| 403 | `FORBIDDEN` | 권한 없음 |
| 404 | `NOT_FOUND` | 리소스 없음 또는 삭제됨 |
| 409 | `CONFLICT` | 재고 부족, 중복 요청 등 |
| 409 | `OPTIMISTIC_LOCK_FAILURE` | 낙관적 락 충돌 (재고 동시 접근) |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |
| 502 | `FEIGN_CLIENT_ERROR` | 서비스 간 통신 오류 |
