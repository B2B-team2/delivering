# 01 - Functional Spec

> 기능 목록, 액터 역할(Role), 비기능 요구사항

---

## 1. 액터 및 역할(Role)

| 역할 코드 | 설명 |
|---|---|
| `MASTER` | 모든 기능에 대한 전체 권한 (시스템 관리자) |
| `HUB_MANAGER` | 담당 허브의 배송담당자·업체·상품 관리 |
| `HUB_DELIVERY_MANAGER` | 허브 간 배송 전담. 허브 → 업체 배송 불가 |
| `COMPANY_DELIVERY_MANAGER` | 최종 허브 → 수령업체 배송 전담. 허브 간 배송 불가 |
| `COMPANY_MANAGER` | 소속 업체 정보 및 등록 상품 관리 |

---

## 2. 기능 목록

### 2.1 User Service — 회원 및 권한

| 기능 | 접근 권한 | 설명 |
|---|---|---|
| 회원가입 | 누구나 | 가입 요청 (approval_status = PENDING) |
| 로그인 | APPROVED 사용자 | JWT 발급 |
| 로그아웃 | 인증 사용자 | 토큰 무효화 |
| 토큰 재발급 | 인증 사용자 | Refresh Token 기반 재발급 |
| 가입 승인/거절 | MASTER, HUB_MANAGER | PENDING → APPROVED / REJECTED |
| 가입 대기 목록 조회 | MASTER, HUB_MANAGER | PENDING 상태 사용자 목록 |
| 전체 사용자 조회 | MASTER | 관리자용 사용자 목록 |
| 사용자 상세 조회 | 본인, MASTER | 사용자 정보 조회 |
| 사용자 정보 수정 | 본인, MASTER | 이름·연락처·슬랙ID 수정 |
| 계정 삭제 | 본인, MASTER | Soft Delete |
| 배송담당자 목록 조회 | MASTER, HUB_MANAGER | 배송담당자 전체 목록 |
| 배송담당자 단건 조회 | MASTER, HUB_MANAGER(담당 허브), 본인 | 배송담당자 상세 (업체담당자 접근 불가) |
| 배송담당자 상태 변경 | MASTER, HUB_MANAGER | 근무 상태 변경 |
| 배송담당자 배정 | 시스템 내부 | 순번 기반 자동 배정 |

### 2.2 Company Service — 업체 및 상품

| 기능 | 접근 권한 | 설명 |
|---|---|---|
| 업체 등록 | MASTER, HUB_MANAGER | 신규 업체(생산/수령) 등록 |
| 업체 목록 조회 | 인증 사용자 | 전체 업체 목록 (검색) |
| 업체 상세 조회 | 인증 사용자 | 단건 업체 정보 |
| 업체 정보 수정 | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 업체 정보 수정 |
| 업체 삭제 | MASTER, HUB_MANAGER | Soft Delete |
| 카테고리 CRUD | MASTER, HUB_MANAGER | 상품 분류 관리 |
| 상품 등록 | MASTER, HUB_MANAGER, COMPANY_MANAGER | 업체 소속 상품 등록 |
| 상품 목록/상세 조회 | 인증 사용자 | 상품 검색 및 조회 |
| 상품 정보 수정 | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | 상품 정보 수정 |
| 상품 판매상태 변경 | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | ON_SALE / SOLD_OUT |
| 상품 삭제 | MASTER, HUB_MANAGER | Soft Delete |
| 상품 옵션 CRUD | MASTER, HUB_MANAGER, COMPANY_MANAGER(소속) | SKU 단위 옵션 관리 |
| 배송지 CRUD | COMPANY_MANAGER(소속) | 수령업체 배송지 관리 |

### 2.3 Hub Service — 허브 및 재고

| 기능 | 접근 권한 | 설명 |
|---|---|---|
| 허브 등록/수정/삭제 | MASTER | 물류 허브 관리 |
| 전체/단건 허브 조회 | 인증 사용자 | Redis 캐싱 적용 |
| 허브 연결 경로 CRUD | MASTER | 허브 간 이동 정보 관리 |
| 허브 경로 탐색 | 내부 서비스 | 경유 포함 경로 탐색 (POST) |
| 물류 창고 CRUD | MASTER, HUB_MANAGER | 창고 관리 |
| 재고 등록/조회 | MASTER, HUB_MANAGER | SKU별 재고 관리 |
| 재고 수동 조정 | MASTER, HUB_MANAGER | 관리자 수동 재고 조정 |
| 재고 예약 | 시스템 내부 | 주문 생성 시 자동 예약 (낙관적 락) |
| 재고 변동 이력 조회 | MASTER, HUB_MANAGER | append-only 이력 조회 |

### 2.4 Order Service — 주문 및 결제

| 기능              | 접근 권한 | 설명 |
|-----------------|---|---|
| 임시주문 항목추가/수정/삭제 | COMPANY_MANAGER | order_drafts 관리 |
| 임시주문 조회         | 본인 | 임시 저장 목록 조회 |
| 주문 생성           | 모든 인증 사용자 (MASTER, HUB_MANAGER, 배송담당자, 업체담당자) | 재고 예약 + 배송 자동 생성 |
| 전체 주문 조회        | MASTER, HUB_MANAGER | 주문 목록 검색 |
| 주문 상세 조회        | 관련 업체 담당자, MASTER | 단건 주문 상세 |
| 서브 주문 상세 조회     | 관련 업체 담당자, MASTER | company_order 단건 |
| 주문 전체 취소        | COMPANY_MANAGER(요청자), MASTER | 전체 주문 취소 + 재고 복원 |
| 서브 주문 부분 취소     | COMPANY_MANAGER(요청자), MASTER | 업체별 주문 부분 취소 |
| 결제 요청           | COMPANY_MANAGER | 결제 준비 (PG 연동) |
| 결제 승인 및 검증      | COMPANY_MANAGER | PG 결제 승인 처리 |
| 결제 취소/환불        | MASTER, COMPANY_MANAGER | 결제 취소 |
| 결제 내역 조회        | 관련 사용자, MASTER | 결제 이력 조회 |

### 2.5 Delivery Service — 배송

| 기능 | 접근 권한 | 설명 |
|---|---|---|
| 배송 생성 | 시스템 내부 | 주문 생성 시 자동 생성 + 경로 일괄 생성 |
| 배송 목록/상세 조회 | 관련 담당자, MASTER | 배송 조회 |
| 배송 조회 (송장 번호) | 인증 사용자 | 트래킹 번호 기반 조회 |
| 배송 상태 변경 | 담당 배송매니저, MASTER | PENDING → SHIPPING → COMPLETED |
| 배송 취소 | MASTER, HUB_MANAGER | PENDING 상태에서만 허용 |
| 세부 경로 조회 | 관련 담당자, MASTER | 구간별 경로 상태 조회 |
| 경로 구간 상태 업데이트 | 담당 배송매니저 | PENDING → MOVING → ARRIVED |
| 담당자 재배정 | MASTER, HUB_MANAGER | 배정 변경 + 이벤트 로그 자동 INSERT |
| 배송 상태 이력 조회 | 관련 담당자, MASTER | delivery_log append-only 조회 |

### 2.6 Operations Service — 운영 지원

| 기능 | 접근 권한 | 설명 |
|---|---|---|
| 클레임 접수 | COMPANY_MANAGER | 반품/교환 접수 (REQUESTED) |
| 클레임 목록/상세 조회 | MASTER, HUB_MANAGER | 클레임 현황 조회 |
| 클레임 상태 업데이트 | MASTER, HUB_MANAGER | PROCESSING → COMPLETED / REJECTED |
| 슬랙 메시지 발송 요청 | 모든 인증 사용자, 시스템 내부 | Slack Incoming Webhook 발송 |
| 슬랙 메시지 이력 조회 | MASTER | 발송 이력 조회 (수정·삭제 불가) |
| AI 발송 시한 예측 | 시스템 내부 | Gemini API 기반 final_deadline_at 산출 |
| AI 요청 로그 조회 | MASTER | 요청/응답 전문 조회 |

---

## 3. 비기능 요구사항

### 3.1 성능
- 허브·경로 정보는 Redis Cache-Aside 전략으로 조회 성능 확보
- 재고 동시 처리는 낙관적 락(`version`) 적용으로 충돌 최소화

### 3.2 가용성 및 확장성
- Eureka 기반 서비스 디스커버리로 동적 라우팅 및 부하 분산
- 서비스별 독립 스키마로 논리적 데이터 격리

### 3.3 데이터 정합성
- FeignClient 호출 실패 시 재시도 로직 적용
- 주문 생성 시 **Saga 오케스트레이션 패턴** 적용: Hub ID 조회 → 재고 예약 → 배송 생성 순차 호출, 실패 시 역순 보상 트랜잭션(LIFO) 실행
- 각 외부 호출은 독립 트랜잭션으로 분리

### 3.4 관찰 가능성 (Observability)
- Zipkin 분산 추적 전 서비스 적용
- Swagger (springdoc-openapi) 서비스별 API 문서화 + Gateway 통합

### 3.5 보안
- Keycloak JWT 발급/비밀번호 암호화
- Spring Security 권한 검증 (API Gateway 레벨)
- 모든 API `Authorization: Bearer {token}` 필수 (회원가입·로그인 제외)

### 3.6 데이터 보존
- 모든 엔티티 Soft Delete (`deleted_at`, `deleted_by`)
- 재고 변동 이력 append-only (`p_inventory_histories`)
- 배송 이벤트 로그 append-only (`p_delivery_log`)

