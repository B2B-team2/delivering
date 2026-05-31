# 00 - Overview

> 스파르타 로지스틱스 (Sparta Logistics) — MSA 기반 국내 B2B 물류 관리 및 배송 시스템

---

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 프로젝트명 | 스파르타 로지스틱스 (Sparta Logistics) |
| 주제 | MSA 기반 국내 B2B 물류 관리 및 배송 시스템 |
| 목표 | 기업 간(B2B) 물류 흐름을 허브 네트워크로 효율적으로 관리하고, AI 기반 배송 시한 예측 기능 제공 |
| 아키텍처 | MSA (Microservices Architecture), Layered Architecture |
| 작성일 | 2026-05-14 |

### 운영 구조

- 전국 **17개 허브 센터** 기반으로 업체 간 상품 주문 및 배송 처리
- 배송 흐름: `공급업체 → 출발 허브 → (경유 허브) → 도착 허브 → 수령업체`
- 허브 경로 모델: **P2P + Hub to Hub Relay**
- 모든 엔티티 **Soft Delete** 처리 (`deleted_at`, `deleted_by`)
- AI 연동: Spring AI + Gemini API로 최종 발송 시한 자동 계산 및 슬랙 알림 발송

---

## 2. 문서 인덱스

| 파일 | 설명 |
|---|---|
| [01-functional-spec.md](01-functional-spec.md) | 기능 목록, 액터 Role, 비기능 요구사항 |
| [02-domain-spec.md](02-domain-spec.md) | 도메인 목록, 상태/흐름, 권한 체계, 도메인 관계 |
| [03-data-spec.md](03-data-spec.md) | 공통 규칙, ERD, 테이블 명세, 인덱스 가이드 |
| [04-api-spec.md](04-api-spec.md) | 공통 규약, 도메인별 엔드포인트, 요청/응답 예시, 에러 코드 |
| [05-service-spec.md](05-service-spec.md) | 서비스 로직 명세, 주요 유스케이스 핵심 흐름 |
| [06-infra-spec.md](06-infra-spec.md) | 인프라 구성도, 기술 스택, CI 흐름 |
| [07-code-design.md](07-code-design.md) | 멀티 모듈 구조, common 모듈, 도메인별 핵심 엔티티, FeignClient, ArchUnit, Checkstyle |
| [08-api-test-pipeline.md](08-api-test-pipeline.md) | OpenAPI 명세 생성 및 Postman 동기화 가이드 |

---

## 3. 마이크로서비스 구성 요약

| 서비스 | 포트 | 스키마 | 핵심 역할 |
|---|---|---|---|
| eureka-server | 8761 | - | 서비스 디스커버리 |
| api-gateway | 8080 | - | 라우팅, JWT 인증/인가 |
| user-service | 19091 | USER | 인증/인가, 역할 관리, 승인 기반 가입 |
| company-service | 19092 | COMPANY | 업체/상품 마스터 데이터 관리 |
| hub-service | 19093 | HUB | 허브·경로 관리, 재고 관리, Redis 캐싱 |
| order-service | 19094 | ORDER | 주문·결제 관리, 임시 주문 |
| delivery-service | 19095 | DELIVERY | 배송 추적, 경로 관리, 이벤트 로그 |
| operations-service | 19096 | OPS | AI 발송 시한 산출, 슬랙 알림, 클레임 관리 |

---

## 4. 핵심 규약 요약

### 공통 응답 형식
```
{ "status": 200, "message": "SUCCESS", "data": { } }
```

### 페이지네이션
- `size`: 10 / 30 / 50 만 허용 (그 외 기본 10건)
- 기본 정렬: `createdAt,DESC`

### Soft Delete
- 모든 엔티티 `deleted_at` 기반 논리적 삭제
- 조회 시 `deleted_at IS NULL` 필터 적용

### 서비스 간 통신
- FeignClient 기반 REST 동기 호출
- 실패 시 재시도 로직 적용
- 서비스 간 내부 호출 시 `X-Gateway-Secret` 헤더로 신뢰 검증
- Saga 오케스트레이션 패턴 적용 (보상 트랜잭션)

### 동시성 제어
- 재고 처리 시 낙관적 락 (`version` 컬럼) 적용

---

## 5. Git / 협업 컨벤션 요약

### 브랜치 전략

```
main ← develop ← dev/[이름]
```

| 브랜치 | 역할 |
|---|---|
| `main` | 최종 배포용 (직접 작업 금지) |
| `develop` | 개발 통합 브랜치 |
| `dev/[이름]` | 개인 작업 브랜치 |

### 커밋 메시지 형식

```
[타입]: [작업내용요약]

예) feat: 주문 생성 API 구현
    fix: 재고 예약 롤백 오류 수정
```

| 타입 | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 구조 개선 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 |
| `chore` | 빌드·환경 설정 변경 |
| `style` | 코드 포맷팅 |

### PR 규칙
- PR 제목: 커밋 메시지 형식과 동일
- 최소 **1명 이상의 팀원 승인(approve)** 후 `develop` 병합
- Discord 알림 자동 연동
