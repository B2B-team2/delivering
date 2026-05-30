# 06 - Infra Spec

> 인프라 구성도, 기술 스택, CI 흐름

---

## 1. 전체 아키텍처

```
[Client]
    │
    ▼
[API Gateway :8080]  ← Spring Cloud Gateway
    │  JWT 검증 (Keycloak :18080에서 발급된 토큰)
    │  라우팅
    ▼
[Eureka Server :8761]  ← 서비스 디스커버리
[Config Server :8888]  ← 공통 설정 중앙 관리
    │
    ├──► [user-service    :19091] ──► PostgreSQL :25432
    ├──► [company-service :19092] ──► PostgreSQL/PostGIS :25435
    ├──► [hub-service     :19093] ──► PostgreSQL/PostGIS :25434
    │                             └─► Redis :26379 (허브/경로 캐시)
    ├──► [order-service   :19094] ──► PostgreSQL :25433
    ├──► [delivery-service:19095] ──► PostgreSQL :25436
    └──► [operations-service:19096] ──► PostgreSQL :25437
                                    ──► Gemini API
                                    └──► Slack Webhook

[Keycloak  :18080] ← JWT 발급·비밀번호 암호화
[Redis     :26379] ← 허브/경로 캐싱
[Zipkin    :9411]  ← 분산 추적
[Docker / Docker Compose] ← 컨테이너화
  └── docker-compose.yml      (애플리케이션 서비스)
  └── docker-compose.infra.yml (인프라: DB, Redis, Keycloak)
```

### 핵심 설계 원칙

| 원칙 | 내용 |
|---|---|
| Gateway & Discovery | Spring Cloud Gateway + Eureka 동적 라우팅 및 부하 분산 |
| Auth | Keycloak JWT 발급·비밀번호 암호화, Gateway에서 토큰 검증 |
| Caching Strategy | 허브(`p_logistics_hubs`)·경로(`p_hub_routes`) 정보를 Redis에 Cache-Aside 전략으로 저장 |
| Data Consistency | Saga 오케스트레이션 + FeignClient 재시도 로직, 주문 생성 시 역순 보상 트랜잭션 |
| Observability | Zipkin 분산 추적 전 서비스 적용 |

---

## 2. 기술 스택

| 구분 | 기술 | 버전/설정 | 비고 |
|---|---|---|---|
| Language | Java | 17 (LTS) | |
| Framework | Spring Boot | 3.3.5 | |
| Security | Spring Security + JWT | Keycloak | Keycloak: JWT 발급·비밀번호 암호화 / Spring Security: 권한 검증 |
| ORM | JPA / Hibernate | | |
| Database | PostgreSQL + PostGIS | 15+ | 서비스별 독립 인스턴스 물리적 분리, 지리 데이터 처리 |
| Cache | Redis | | 허브·경로 캐싱 (Cache-Aside) |
| Service Discovery | Spring Cloud Eureka | | |
| API Gateway | Spring Cloud Gateway | | JWT 인증/인가 |
| Service Comm. | FeignClient | | 서비스 간 REST 호출, 실패 시 재시도 |
| Auth | Keycloak | latest | JWT 발급·비밀번호 암호화 (포트 18080) |
| Build | Gradle | 8.x | Multi-module |
| API Docs | Swagger (springdoc-openapi) | | 서비스별 문서화 + Gateway 통합 |
| AI | Google Gemini API | | Spring AI 연동 |
| 알림 | Slack Incoming Webhook | | |
| 분산 추적 | Zipkin | | 전 서비스 적용 |
| 컨테이너 | Docker / Docker Compose | | 로컬 환경 실행 |
| 동시성 제어 | Optimistic Lock (version) | | 재고 차감 시 낙관적 락 |

---

## 3. 서비스별 포트 및 스키마

| 서비스 | 앱 포트 | DB 컨테이너 | DB 호스트 포트 |
|---|:---:|---|:---:|
| eureka-server | 8761 | - | - |
| config-server | 8888 | - | - |
| api-gateway | 8080 | - | - |
| keycloak | 18080 | - | - |
| redis | - | msa-redis | 26379 |
| zipkin | 9411 | - | - |
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
Order Service ──FeignClient──► Company Service   (Hub ID 일괄 조회, 상품 옵션 상세 조회)
Order Service ──FeignClient──► Hub Service       (재고 예약/취소/차감)
Order Service ──FeignClient──► Delivery Service  (배송 생성/취소)
Order Service ──FeignClient──► Operations Service (AI 발송 시한 계산)

Hub Service   ──FeignClient──► Order Service     (출고 준비 / 출고 완료 주문 상태 변경)
Hub Service   ──FeignClient──► Company Service   (허브 내 업체 존재 여부 확인)

Delivery Service ──FeignClient──► Hub Service    (경로 탐색)
Delivery Service ──FeignClient──► User Service   (배송담당자 자동 배정)
Delivery Service ──FeignClient──► Order Service  (배송 완료 → company_order 상태 갱신)

Operations Service ──FeignClient──► Hub Service  (반품 재고 복원)
Operations Service ──FeignClient──► Order Service (클레임 주문 취소)
```

> `Order Service → Company Service` 중 상품 옵션 조회는 FeignClient name이 `"product-service"`로 등록되어 있으나 실제 호출 대상은 company-service의 `/internal/product-options/details` 엔드포인트.

**장애 처리**: 실패 시 재시도 로직 적용, 주요 흐름은 Saga 보상 트랜잭션으로 일관성 유지


---

## 6. 데이터베이스 구성

### PostgreSQL + PostGIS
- 서비스별 **독립 PostgreSQL 인스턴스** (물리적 분리)
- hub-service, company-service는 PostGIS 확장 적용 (지리 데이터 처리)
- 지리 데이터(위도/경도): `GEOMETRY(Point, 4326)` 단일 컬럼 (`location`)

### DB 구조
```
docker-compose.infra.yml
├── user-service       → my-postgres         (postgres:16-alpine,  :25432)
├── order-service      → order_service_db    (postgres:16-alpine,  :25433)
├── hub-service        → hub_service_db      (postgis/postgis:16-3.4, :25434)
├── company-service    → company_service_db  (postgis/postgis:16-3.4, :25435)
├── delivery-service   → delivery_service_db (postgres:16-alpine,  :25436)
└── operations-service → operations_service_db (postgres:16-alpine, :25437)
```

---

## 7. CI/CD 흐름

### 전체 파이프라인

```
[개발자]
    │ git push → dev/[이름]
    ▼
[GitHub PR 생성] (dev/[이름] → develop)
    │
    ├── pr.yml: PR 오픈 Discord 알림
    │
    ├── ci.yml: CI 파이프라인 실행
    │     ├── Checkout
    │     ├── Java 17 (Temurin) 설정
    │     ├── ./gradlew clean build (테스트 포함)
    │     └── Discord 알림 (시작 / 성공 / 실패)
    │
    ▼
[PR 리뷰] — 팀원 1명 이상 Approve
    │
    ▼
[develop 브랜치 머지]
    │
    ├── pr.yml: PR 머지 Discord 알림
    │
    ├── ci.yml: develop 브랜치 CI 재실행
    │
    └── cd.yml: CD 파이프라인 자동 실행
          ├── SSH 접속 (서버)
          ├── git pull origin develop
          ├── ./gradlew clean build -x test
          ├── docker compose up --build -d
          └── Discord 알림 (시작 / 성공 / 실패)
```

### 워크플로 요약

| 파일 | 트리거 | 주요 동작 |
|---|---|---|
| `ci.yml` | push / PR → `main`, `develop` | Gradle 빌드 + 테스트, Discord 알림 |
| `cd.yml` | push → `develop` (머지 후 자동) | SSH 서버 배포, Docker Compose 재시작, Discord 알림 |
| `pr.yml` | PR opened / merged → `main`, `develop` | PR 오픈·머지 Discord 알림 |

### 브랜치 보호 규칙
- `main`, `develop` 브랜치 직접 push 금지
- `develop` 브랜치 force push 차단
- PR 최소 1명 Approve 필수