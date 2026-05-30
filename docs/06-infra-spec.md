# 06 - Infra Spec

> 인프라 구성도, 기술 스택, CI 흐름

---

## 1. 전체 아키텍처

```
[Client]
    │
    ▼
[API Gateway :8080]  ← Spring Cloud Gateway
    │  JWT 인증/인가 (Keycloak)
    │  라우팅
    ▼
[Eureka Server :8761]  ← 서비스 디스커버리
    │
    ├──► [user-service    :19091]
    ├──► [company-service :19092]
    ├──► [hub-service     :19093]  ←─ Redis (캐시)
    ├──► [order-service   :19094]
    ├──► [delivery-service:19095]
    └──► [operations-service:19096] ──► Gemini API
                                    └──► Slack Webhook

[Config Server] ← 공통 설정 중앙 관리
[PostgreSQL + PostGIS] ← 스키마 분리 (6개)
[Redis] ← 허브/경로 캐싱
[Zipkin] ← 분산 추적
[Docker / Docker Compose] ← 컨테이너화
```

### 핵심 설계 원칙

| 원칙 | 내용 |
|---|---|
| Gateway & Discovery | Spring Cloud Gateway + Eureka 동적 라우팅 및 부하 분산 |
| Caching Strategy | 허브(`p_logistics_hubs`)·경로(`p_hub_routes`) 정보를 Redis에 Cache-Aside 전략으로 저장 |
| Data Consistency | FeignClient 통신 시 트랜잭션 관리 및 실패 재시도 로직 |
| Observability | Zipkin 분산 추적 전 서비스 적용 |

---

## 2. 기술 스택

| 구분 | 기술 | 버전/설정 | 비고 |
|---|---|---|---|
| Language | Java | 17 (LTS) | |
| Framework | Spring Boot | 3.3.5 | |
| Security | Spring Security + JWT | Keycloak | Keycloak: JWT 발급·비밀번호 암호화 / Spring Security: 권한 검증 |
| ORM | JPA / Hibernate | | |
| Database | PostgreSQL + PostGIS | 16 | 서비스별 물리적 분리 (독립 컨테이너), 지리 데이터 처리 |
| Cache | Redis | | 허브·경로 캐싱 (Cache-Aside) |
| Service Discovery | Spring Cloud Eureka | | |
| API Gateway | Spring Cloud Gateway | | JWT 인증/인가 |
| Service Comm. | FeignClient | | 서비스 간 REST 호출, 실패 시 재시도 |
| Build | Gradle | 8.x | Multi-module |
| API Docs | Swagger (springdoc-openapi) | | 서비스별 문서화 + Gateway 통합 |
| AI | Google Gemini API | | Spring AI 연동 |
| 알림 | Slack Incoming Webhook | | |
| 분산 추적 | Zipkin | | 전 서비스 적용 |
| 컨테이너 | Docker / Docker Compose | | 로컬 환경 실행 |
| 동시성 제어 | Optimistic Lock (version) | | 재고 차감 시 낙관적 락 |

---

## 3. 서비스별 포트 및 DB 구성

| 서비스 | 포트 | DB 컨테이너 | DB 포트 |
|---|---|---|---|
| eureka-server | 8761 | - | - |
| config-server | 8888 | - | - |
| api-gateway | 8080 | - | - |
| user-service | 19091 | my-postgres | 25432 |
| company-service | 19092 | company_service_db (PostGIS) | 25435 |
| hub-service | 19093 | hub_service_db (PostGIS) | 25434 |
| order-service | 19094 | order_service_db | 25433 |
| delivery-service | 19095 | delivery_service_db | 25436 |
| operations-service | 19096 | operations_service_db | 25437 |

---

## 4. Redis 캐싱 전략

**Cache-Aside (Lazy Loading)** 패턴 적용

```
[조회 요청]
    │
    ▼
Redis에 캐시 존재? ──YES──► 캐시 반환
    │ NO
    ▼
DB 조회 → Redis에 저장 → 반환
```

**캐싱 대상**
- `p_logistics_hubs`: 17개 허브 정보 (변경 빈도 낮음)
- `p_hub_routes`: 허브 간 이동 경로 정보 (변경 빈도 낮음)

**캐시 무효화**: 허브·경로 수정/삭제 시 해당 캐시 즉시 evict

---

## 5. 서비스 간 통신

### FeignClient (동기)

```
Order Service ──FeignClient──► Hub Service       (재고 예약/취소/차감)
Order Service ──FeignClient──► Delivery Service  (배송 생성)
Order Service ──FeignClient──► Operations Service (AI 발송 시한 계산)

Delivery Service ──FeignClient──► Hub Service    (경로 탐색)
Delivery Service ──FeignClient──► User Service   (배송담당자 배정)
Delivery Service ──FeignClient──► Order Service  (배송 완료 → 주문 완료)

Operations Service ──FeignClient──► Hub Service  (반품 재고 복원)
Operations Service ──FeignClient──► Order Service (주문 취소)
```

**장애 처리**: 실패 시 재시도 로직 적용, 주요 흐름은 트랜잭션 롤백으로 일관성 유지

### 내부 서비스 인증 (X-Gateway-Secret)
- FeignClient 호출 시 `GatewayFeignInterceptor`가 `X-Gateway-Secret` 헤더를 자동 주입
- 각 서비스의 `CustomPreAuthFilter`가 해당 헤더를 검증하여 직접 접근 차단
- 시크릿 값은 환경변수 `GATEWAY_SECRET`으로 관리

---

## 6. 데이터베이스 구성

### 물리적 분리 (서비스별 독립 컨테이너)
각 마이크로서비스는 **독립된 PostgreSQL 컨테이너**를 가집니다.
서비스 간 DB 직접 접근이 구조적으로 불가능하며, 지리 데이터가 필요한 서비스(hub, company)는 PostGIS 이미지를 사용합니다.

```
[my-postgres         :25432] ← user-service    (postgres:16-alpine)
[order_service_db    :25433] ← order-service   (postgres:16-alpine)
[hub_service_db      :25434] ← hub-service     (postgis:16-3.4)
[company_service_db  :25435] ← company-service (postgis:16-3.4)
[delivery_service_db :25436] ← delivery-service(postgres:16-alpine)
[operations_service_db:25437]← operations-service(postgres:16-alpine)
```

### 주요 테이블 구성
```
user-service (my-postgres / postgres:16-alpine)
  └── p_users, p_admins, p_hub_managers, p_delivery_managers, p_company_managers

company-service (company_service_db / postgis:16-3.4)
  └── p_companies, p_product_categories, p_products, p_product_options, p_delivery_addresses

hub-service (hub_service_db / postgis:16-3.4)
  └── p_logistics_hubs, p_hub_routes, p_warehouses, p_warehouse_inventory, p_inventory_histories

order-service (order_service_db / postgres:16-alpine)
  └── p_orders, p_company_orders, p_order_items, p_order_drafts, p_payments

delivery-service (delivery_service_db / postgres:16-alpine)
  └── p_deliveries, p_delivery_routes, p_delivery_log

operations-service (operations_service_db / postgres:16-alpine)
  └── p_order_claims, p_slack_messages, p_ai_requests
```

---

## 7. CI 흐름

```
[개발자]
    │ git push → dev/[이름]
    ▼
[GitHub]
    │
    ├── PR 생성 (dev/[이름] → dev)
    │
    ▼
[CI Pipeline]
    │
    ├── 코드 빌드 (Gradle)
    ├── 단위 테스트 실행
    └── 결과 Discord 알림
    │
    ▼
[PR 리뷰]
    │ 팀원 1명 이상 Approve
    ▼
[dev 브랜치 머지]
    │
    ▼
[Discord 알림] ← 머지 완료 알림
```

**브랜치 보호 규칙**
- `main`, `dev` 브랜치 직접 push 금지
- `dev` 브랜치 force push 차단
- PR 최소 1명 Approve 필수

---

## 8. 로컬 환경 실행

Docker Compose로 전체 인프라 일괄 실행

인프라와 앱 서비스를 두 파일로 분리하여 관리합니다.

```yaml
# docker-compose.infra.yml — 인프라 (DB / Redis / Keycloak)
services:
  my-postgres:    # PostgreSQL (user-service 전용)
  company_service_db:  # PostgreSQL + PostGIS
  hub_service_db:      # PostgreSQL + PostGIS
  order_service_db:    # PostgreSQL
  delivery_service_db: # PostgreSQL
  operations_service_db: # PostgreSQL
  msa-redis:      # Redis
  my-keycloak:    # Keycloak (JWT 발급)
  zipkin:         # 분산 추적

# docker-compose.yml — 앱 서비스
services:
  config-server:  # Spring Cloud Config
  eureka-server:  # 서비스 디스커버리
  api-gateway:    # API Gateway
  user-service:
  company-service:
  hub-service:
  order-service:
  delivery-service:
  operations-service:
```

**실행 순서**
1. `docker compose -f docker-compose.infra.yml up -d` (DB, Redis, Keycloak, Zipkin)
2. `docker compose up -d --build` (앱 서비스)
3. config-server → eureka-server → 나머지 서비스 순으로 healthcheck 기반 자동 기동
