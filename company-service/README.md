# company-service

거래처(업체)와 거래처가 등록한 상품/상품옵션/카테고리를 관리하는 서비스입니다.

## 주요 기능
- 거래처(Company) 등록/조회/수정/삭제 및 거래처 배송지(주소) 등록/조회/수정/삭제
- 상품(Product), 상품옵션(ProductOption), 상품 카테고리(ProductCategory) 등록/조회/수정/상태변경/삭제
- `@PreAuthorize`를 통한 역할 기반 접근 제어(MASTER, HUB_MANAGER, COMPANY_MANAGER 등) 및 소유자 검증(`@authService.isCompanyOwner` 등)
- 다른 서비스(hub-service, order-service 등)가 호출하는 내부 API: 거래처-허브 매핑, 거래처 존재 확인, 기본 배송지 조회, 상품옵션 상세 조회
- Resilience4j 서킷브레이커/타임리미터/재시도 설정(companyCircuitBreaker 등) 보유

## 기술 스택
- Spring Web, Spring Boot Actuator
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client
- Spring Data JPA, Spring Security
- springdoc-openapi-starter-webmvc-ui
- Resilience4j (Circuit Breaker / Time Limiter / Retry), Spring AOP
- Micrometer Tracing (Brave) + Zipkin Reporter
- PostgreSQL, Flyway, Hibernate Spatial, JTS(locationtech)
- Testcontainers(PostgreSQL), H2(테스트)

## API
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/companies | 거래처 생성 |
| GET | /api/v1/companies/{companyId} | 거래처 조회 |
| PATCH | /api/v1/companies/{companyId} | 거래처 수정 |
| DELETE | /api/v1/companies/{companyId} | 거래처 삭제 |
| POST | /api/v1/companies/{companyId}/addresses | 거래처 배송지 등록 |
| GET | /api/v1/companies/{companyId}/addresses | 거래처 배송지 목록 조회 |
| PATCH | /api/v1/companies/addresses/{addressId} | 거래처 배송지 수정 |
| DELETE | /api/v1/companies/addresses/{addressId} | 거래처 배송지 삭제 |
| POST | /api/v1/products | 상품 생성 |
| GET | /api/v1/products | 상품 목록 조회 |
| GET | /api/v1/products/{productId} | 상품 조회 |
| PATCH | /api/v1/products/{productId} | 상품 수정 |
| PATCH | /api/v1/products/{productId}/status | 상품 상태 변경 |
| DELETE | /api/v1/products/{productId} | 상품 삭제 |
| POST | /api/v1/product-options | 상품옵션 생성 |
| GET | /api/v1/product-options | 상품옵션 목록 조회 |
| GET | /api/v1/product-options/{productOptionId} | 상품옵션 조회 |
| PATCH | /api/v1/product-options/{productOptionId} | 상품옵션 수정 |
| DELETE | /api/v1/product-options/{productOptionId} | 상품옵션 삭제 |
| POST | /api/v1/categories | 카테고리 생성 |
| GET | /api/v1/categories | 카테고리 목록 조회 |
| GET | /api/v1/categories/{categoryId} | 카테고리 조회 |
| PATCH | /api/v1/categories/{categoryId} | 카테고리 수정 |
| DELETE | /api/v1/categories/{categoryId} | 카테고리 삭제 |
| POST | /api/v1/internal/companies/hub-mapping | (내부) 거래처-허브 매핑 |
| GET | /api/v1/internal/companies/exists | (내부) 거래처 존재 확인 |
| GET | /api/v1/internal/companies/{companyId}/default-address | (내부) 기본 배송지 조회 |
| POST | /api/v1/internal/product-options/details | (내부) 상품옵션 상세 조회 |

## 아키텍처
`company`, `product` 두 바운디드 컨텍스트가 각각 `domain(core/repository)` / `application(dto/service)` / `infrastructure(repository/adapter)` / `presentation(controller/dto)` 계층으로 분리되어 있고, 공통 설정/예외는 `global` 패키지에 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19092
- 실행: `./gradlew :company-service:bootRun`
