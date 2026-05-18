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
[Apache Kafka] ← 비동기 이벤트
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
| Database | PostgreSQL + PostGIS | 15+ | 스키마별 논리적 분리, 지리 데이터 처리 |
| Cache | Redis | | 허브·경로 캐싱 (Cache-Aside) |
| Service Discovery | Spring Cloud Eureka | | |
| API Gateway | Spring Cloud Gateway | | JWT 인증/인가 |
| Service Comm. | FeignClient | | 서비스 간 REST 호출, 실패 시 재시도 |
| Message Broker | Apache Kafka | | 서비스 간 비동기 이벤트 |
| Build | Gradle | 8.x | Multi-module |
| API Docs | Swagger (springdoc-openapi) | | 서비스별 문서화 + Gateway 통합 |
| AI | Google Gemini API | | Spring AI 연동 |
| 알림 | Slack Incoming Webhook | | |
| 분산 추적 | Zipkin | | 전 서비스 적용 |
| 컨테이너 | Docker / Docker Compose | | 로컬 환경 실행 |
| 동시성 제어 | Optimistic Lock (version) | | 재고 차감 시 낙관적 락 |

---

## 3. 서비스별 포트 및 스키마

| 서비스 | 포트 | DB 스키마 |
|---|---|---|
| eureka-server | 8761 | - |
| config-server | 8888 | - |
| api-gateway | 8080 | - |
| user-service | 19091 | USER |
| company-service | 19092 | COMPANY |
| hub-service | 19093 | HUB |
| order-service | 19094 | ORDER |
| delivery-service | 19095 | DELIVERY |
| operations-service | 19096 | OPS |

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

### Apache Kafka (비동기)
- 서비스 간 이벤트 기반 비동기 처리에 사용
- 실시간성보다 안정적 이벤트 전달이 필요한 경우 활용

---

## 6. 데이터베이스 구성

### PostgreSQL + PostGIS
- 단일 PostgreSQL 인스턴스에 **스키마(Schema)** 단위로 서비스별 논리적 분리
- 지리 데이터(위도/경도): `GEOMETRY(Point, 4326)` 타입 (PostGIS 확장)

### 스키마 구조
```
PostgreSQL
├── schema: USER
│     └── p_users, p_admins, p_hub_managers, p_delivery_managers, p_company_managers
├── schema: COMPANY
│     └── p_companies, p_product_categories, p_products, p_product_options, p_delivery_addresses
├── schema: HUB
│     └── p_logistics_hubs, p_hub_routes, p_warehouses, p_warehouse_inventory, p_inventory_histories
├── schema: ORDER
│     └── p_orders, p_company_orders, p_order_items, p_order_drafts, p_payments
├── schema: DELIVERY
│     └── p_deliveries, p_delivery_routes, p_delivery_log
└── schema: OPS
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

```yaml
# 주요 컨테이너 구성 (docker-compose.yml)
services:
  postgres:       # PostgreSQL + PostGIS
  redis:          # Redis
  kafka:          # Apache Kafka
  zookeeper:      # Kafka 의존
  zipkin:         # 분산 추적
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
1. `postgres`, `redis`, `kafka`, `zipkin` 기동
2. `config-server` 기동
3. `eureka-server` 기동
4. 나머지 마이크로서비스 기동
5. `api-gateway` 기동
