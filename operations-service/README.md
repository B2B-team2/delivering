# operations-service

배송 클레임(반품/분실 등) 처리, Slack 알림 발송, AI 요청 처리를 담당하는 운영 지원 서비스입니다.

## 주요 기능
- 클레임(Claim) 등록/조회/상태 변경, 타 서비스가 조회하는 내부 클레임 조회 API
- Slack 메시지 발송/조회 및 발송 이력 조회
- AI 생성 요청 처리 및 요청 이력 조회
- `@PreAuthorize` 기반 역할별 접근 제어(COMPANY_MANAGER, MASTER, HUB_MANAGER, HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER)
- 클레임 처리 과정에서 Feign으로 hub-service(재고 반품/차감), order-service(주문 상세 조회, 클레임 취소 처리) 호출
- Resilience4j 서킷브레이커/타임리미터/재시도 설정(claimCircuitBreaker 등) 보유

## 기술 스택
- Spring Web, Spring Boot Actuator
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client, Spring Cloud OpenFeign
- Spring Data JPA, Spring Security
- springdoc-openapi-starter-webmvc-ui
- Resilience4j (Circuit Breaker / Time Limiter / Retry), Spring AOP
- Micrometer Tracing(Brave) + Zipkin Reporter
- H2(테스트)

## API
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/claims | 클레임 등록 |
| GET | /api/v1/claims | 클레임 목록 조회 |
| GET | /api/v1/claims/{claimId} | 클레임 조회 |
| PATCH | /api/v1/claims/{claimId}/status | 클레임 상태 변경 |
| GET | /api/v1/internal/claims/{claimId} | (내부) 클레임 조회 |
| POST | /api/v1/slack/send | Slack 메시지 발송 |
| GET | /api/v1/slack/messages | Slack 메시지 목록 조회 |
| GET | /api/v1/slack/send/{message_id} | Slack 발송 이력 조회 |
| POST | /api/v1/ai/generate | AI 생성 요청 |
| GET | /api/v1/ai/requests/{request_id} | AI 요청 조회 |
| GET | /api/v1/ai/requests | AI 요청 목록 조회 |

## 아키텍처
`claim`, `slack`, `ai` 세 바운디드 컨텍스트가 각각 `domain/application/infrastructure/presentation` 계층으로 분리되어 있고, 클레임 처리를 위한 외부 서비스 호출은 `claim/infrastructure/client`에 Feign 클라이언트로 모여 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19096
- 실행: `./gradlew :operations-service:bootRun`
