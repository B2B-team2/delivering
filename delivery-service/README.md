# delivery-service

주문 건에 대한 실제 배송(허브 간 이동 경로 포함)을 생성·추적·관리하는 서비스입니다.

## 주요 기능
- 배송 생성/조회/추적번호 조회/상태 변경(시작·완료)/취소/삭제 및 배송 주소 조회
- 배송 경로(deliveryRoute) 조회, 경로 상태 변경/삭제
- 배송 로그(deliveryLog) 조회
- 동일 주문 건에 대한 동시 배송 생성 요청을 막기 위한 Redisson 분산 락(`DeliveryLockFacade`)
- 배송 추적 조회 결과를 `deliveryTracking` 캐시로 Redis에 캐싱(`@Cacheable`/`@CacheEvict`)하여 상태 변경 시마다 무효화
- Feign 클라이언트로 hub-service(경로 검색), order-service(배송완료 통보), user-service(배송담당자 정보), operations-service(Slack 알림) 호출 — AI 서비스 연동 클라이언트는 코드상 주석 처리되어 비활성 상태

## 기술 스택
- Spring Web, Spring Boot Actuator
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client, Spring Cloud OpenFeign
- Spring Data JPA, Spring Data Redis, Spring Cache, Redisson(redisson-spring-boot-starter)
- springdoc-openapi-starter-webmvc-ui
- Micrometer Tracing(Brave) + Zipkin Reporter
- PostgreSQL, Flyway

## API
| Method | Path | 설명 |
|---|---|---|
| GET | /api/v1/deliveries | 배송 목록 조회 |
| GET | /api/v1/deliveries/{delivery_id} | 배송 조회 |
| GET | /api/v1/deliveries/tracking/{tracking_number} | 추적번호로 배송 조회(캐시) |
| GET | /api/v1/delivery-addresses/{delivery_id}/{address_id} | 배송 주소 조회 |
| PATCH | /api/v1/deliveries/{delivery_id}/status | 배송 상태 변경 |
| PATCH | /api/v1/deliveries/start/{trackingNumber} | 배송 시작 처리 |
| PATCH | /api/v1/deliveries/complete/{trackingNumber} | 배송 완료 처리 |
| PUT | /api/v1/deliveries/{delivery_id}/cancel | 배송 취소 |
| DELETE | /api/v1/deliveries/{deliveryId} | 배송 삭제 |
| GET | /api/v1/deliveries/{delivery_id}/routes | 배송 경로 상세 조회 |
| PATCH | /api/v1/deliveries/{delivery_id}/routes/{route_id} | 배송 경로 상태 변경 |
| DELETE | /api/v1/deliveries/{delivery_id}/routes/{route_id} | 배송 경로 삭제 |
| GET | /api/v1/deliveries/{delivery_id}/logs | 배송 로그 조회 |
| POST | /api/v1/internal/deliveries | (내부) 배송 생성 |
| POST | /api/v1/internal/deliveries/cancel | (내부) 배송 취소 |

## 아키텍처
`delivery`, `deliveryLog`, `deliveryRoute` 세 바운디드 컨텍스트가 각각 `domain/application/infrastructure/presentation` 계층으로 분리되어 있으며, `delivery/infrastructure/client`에 타 서비스 호출용 Feign 클라이언트가 모여 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19095
- 실행: `./gradlew :delivery-service:bootRun`
