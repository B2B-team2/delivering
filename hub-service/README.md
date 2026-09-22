# hub-service

물류 허브, 허브 간 이동 경로, 창고, 재고를 관리하는 서비스입니다.

## 주요 기능
- 허브(Hub) 등록/조회/수정/삭제 및 허브별 경로 목록 조회
- 허브 간 이동 경로(HubRoute) 등록/조회/수정/삭제 및 경로 탐색(`/search`)
- 창고(Warehouse) 등록/조회(허브별 조회 포함)/수정/삭제
- 재고(Inventory) 등록/조회(창고별)/조정/이력 조회/삭제, 거래처 주문 단위 입고 준비·출고 처리
- 다른 서비스가 호출하는 내부 API: 재고 예약/예약취소/거래처 단위 취소/차감/반품, 허브 경로 탐색, 거래처 존재 확인, 주문 준비/배송중 상태 갱신
- 허브(`hubs`)·허브경로(`hubRoutes`) 조회 결과를 Redis 캐시로 저장(`@Cacheable`)하고 생성/수정/삭제 시 무효화(`@CacheEvict`)
- Feign 클라이언트(`CompanyClient`, `OrderClient`)로 company-service/order-service 호출

## 기술 스택
- Spring Web, Spring Boot Actuator
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client, Spring Cloud OpenFeign
- Spring Data JPA, Spring Data Redis, Spring Cache
- Spring Security, Spring Boot Validation
- springdoc-openapi-starter-webmvc-ui
- Micrometer Tracing(Brave) + Zipkin Reporter
- PostgreSQL, Flyway, Hibernate Spatial, JTS(locationtech) — 좌표 기반 허브 위치/경로 처리
- Testcontainers, H2(테스트)

## API
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/hubs | 허브 생성 |
| GET | /api/v1/hubs | 허브 목록 조회 |
| GET | /api/v1/hubs/{hub_id} | 허브 조회 |
| PATCH | /api/v1/hubs/{hub_id} | 허브 수정 |
| DELETE | /api/v1/hubs/{hub_id} | 허브 삭제 |
| GET | /api/v1/hubs/{hub_id}/routes | 허브별 경로 목록 조회 |
| POST | /api/v1/hub-routes | 허브 경로 생성 |
| GET | /api/v1/hub-routes | 허브 경로 목록 조회 |
| GET | /api/v1/hub-routes/{route_id} | 허브 경로 조회 |
| PATCH | /api/v1/hub-routes/{route_id} | 허브 경로 수정 |
| DELETE | /api/v1/hub-routes/{route_id} | 허브 경로 삭제 |
| POST | /api/v1/hub-routes/search | 허브 경로 탐색 |
| POST | /api/v1/warehouses | 창고 생성 |
| GET | /api/v1/warehouses | 창고 목록 조회 |
| GET | /api/v1/warehouses/{warehouse_id} | 창고 조회 |
| GET | /api/v1/warehouses/hub/{hub_id} | 허브별 창고 조회 |
| PATCH | /api/v1/warehouses/{warehouse_id} | 창고 수정 |
| DELETE | /api/v1/warehouses/{warehouse_id} | 창고 삭제 |
| POST | /api/v1/inventory | 재고 생성 |
| GET | /api/v1/inventory/{inventory_id} | 재고 조회 |
| GET | /api/v1/inventory/warehouses/{warehouse_id} | 창고별 재고 조회 |
| GET | /api/v1/inventory/{inventory_id}/histories | 재고 변동 이력 조회 |
| PATCH | /api/v1/inventory/{inventory_id}/adjust | 재고 수량 조정 |
| DELETE | /api/v1/inventory/{inventory_id} | 재고 삭제 |
| PATCH | /api/v1/inventory/company-orders/{companyOrderId}/prepare | 거래처 주문 출고 준비 |
| PATCH | /api/v1/inventory/company-orders/{companyOrderId}/ship | 거래처 주문 출고 처리 |
| POST | /api/v1/internal/hub-routes/search | (내부) 허브 경로 탐색 |
| POST | /api/v1/internal/inventory/reserve | (내부) 재고 예약 |
| POST | /api/v1/internal/inventory/cancel | (내부) 재고 예약 취소 |
| POST | /api/v1/internal/inventory/cancel/company | (내부) 거래처 단위 예약 취소 |
| POST | /api/v1/internal/inventory/deduct | (내부) 재고 차감 |
| POST | /api/v1/internal/inventory/return | (내부) 재고 반품 |

## 아키텍처
`hub`, `hubroute`, `inventory`, `warehouse` 네 바운디드 컨텍스트가 각각 `domain(core/port/repository)` / `application(dto/service)` / `infrastructure(adapter/repository)` / `presentation(controller/dto)` 계층으로 분리되어 있고, 공통 설정·예외·외부 클라이언트는 `global` 패키지에 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19093
- 실행: `./gradlew :hub-service:bootRun`
