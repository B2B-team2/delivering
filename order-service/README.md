# order-service

주문 초안(Draft) 작성부터 정식 주문·결제 처리까지 담당하며, 배송/허브/거래처/상품 서비스를 orchestration하는 서비스입니다.

## 주요 기능
- 주문 초안(Draft) 추가/수정/삭제/조회 및 초안 기반 정식 주문 생성
- 주문(Order) 생성/조회/취소, 거래처 단위 주문(CompanyOrder) 조회/취소
- 결제(Payment) 조회/취소
- 타 서비스가 호출하는 내부 API: 거래처 주문 상세 조회, 배송완료/준비/배송중 상태 갱신, 클레임 취소 처리
- 주문 생성 시 Feign으로 company-service(거래처-허브 매핑/기본주소), product-option 상세(product-service), hub-service(재고 예약/취소), delivery-service(배송 생성/취소) 호출
- JPA `default_batch_fetch_size: 100` 설정으로 LAZY 컬렉션 N+1 조회 방지

## 기술 스택
- Spring Web, Spring Boot Actuator, Spring WebFlux(의존성 포함)
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client, Spring Cloud OpenFeign
- Spring Data JPA, Spring Security, Spring Boot Validation
- springdoc-openapi-starter-webmvc-ui
- Resilience4j (Circuit Breaker / Time Limiter / Retry), Spring AOP
- Micrometer Tracing(Brave) + Zipkin Reporter
- PostgreSQL, Flyway
- Testcontainers(PostgreSQL), Spring Cloud Contract WireMock, H2(테스트)

## API
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/drafts | 주문 초안 생성 |
| GET | /api/v1/drafts | 주문 초안 목록 조회 |
| PATCH | /api/v1/drafts/{draftId} | 주문 초안 수정 |
| DELETE | /api/v1/drafts/{draftId} | 주문 초안 삭제 |
| POST | /api/v1/drafts/orders | 초안 기반 주문 생성 |
| POST | /api/v1/orders | 주문 생성 |
| GET | /api/v1/orders | 주문 목록 조회 |
| GET | /api/v1/orders/{orderId} | 주문 조회 |
| PATCH | /api/v1/orders/{orderId}/cancel | 주문 취소 |
| GET | /api/v1/orders/company/{companyOrderId} | 거래처 주문 조회 |
| PATCH | /api/v1/orders/company/{companyOrderId}/cancel | 거래처 주문 취소 |
| PATCH | /api/v1/payments/{paymentId}/cancel | 결제 취소 |
| GET | /api/v1/payments | 결제 목록 조회 |
| GET | /api/v1/payments/{paymentId} | 결제 조회 |
| GET | /api/v1/internal/orders/company/{companyOrderId}/details | (내부) 거래처 주문 상세 조회 |
| PATCH | /api/v1/internal/orders/company/{companyOrderId}/delivered | (내부) 배송완료 처리 |
| PATCH | /api/v1/internal/orders/company/{companyOrderId}/claim-cancel | (내부) 클레임 취소 처리 |
| PATCH | /api/v1/internal/orders/company/{companyOrderId}/preparing | (내부) 준비중 상태 갱신 |
| PATCH | /api/v1/internal/orders/company/{companyOrderId}/shipped | (내부) 배송중 상태 갱신 |

## 아키텍처
`draft`, `order`, `payment` 세 바운디드 컨텍스트가 각각 `domain(core/event/repository)` / `application(dto/port/service)` / `infrastructure(client/feign/repository)` / `presentation(controller/dto)` 계층으로 분리되어 있고, 외부 서비스 연동은 `order/infrastructure/client`, `order/infrastructure/feign`에 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19094
- 실행: `./gradlew :order-service:bootRun`
