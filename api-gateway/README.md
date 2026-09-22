# api-gateway

sparta-logistics(물류 플랫폼)의 단일 진입점(API Gateway)으로, 외부 요청을 Eureka에 등록된 각 백엔드 서비스로 라우팅하고 JWT 인증을 수행하는 모듈입니다.

## 주요 기능
- Spring Cloud Gateway 기반 라우팅: user-service, order-service, company-service, delivery-service, hub-service, operations-service 6개 서비스로 요청 분기
- Keycloak이 발급한 JWT를 OAuth2 Resource Server + `JwtDecoder`로 검증하는 글로벌 필터(`JwtAuthenticationFilter`)
- 인증 성공 시 `X-User-Id`, `X-User-Email`, `X-User-Role`, `X-Company-Id`, `X-Hub-Id`, `X-Gateway-Secret` 헤더를 downstream 서비스로 전파
- 로그인/회원가입/토큰갱신/Swagger/actuator 등 화이트리스트 경로는 인증 예외 처리
- 각 서비스의 `/{service}/v3/api-docs`를 `/v3/api-docs`로 `RewritePath`하여 서비스별 OpenAPI 문서 통합 노출

## 기술 스택
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Spring Boot Actuator
- Spring Boot OAuth2 Resource Server / Spring Security OAuth2 JOSE
- springdoc-openapi-starter-webflux-ui

## API
자체 REST 컨트롤러 없이 라우팅만 수행합니다. config-server에 정의된 라우팅 테이블은 다음과 같습니다.

| 라우트 ID | 대상 서비스 | 매칭 Path |
|---|---|---|
| user-service-route | lb://USER-SERVICE | /api/v1/auth/**, /api/v1/users/**, /api/v1/admin/**, /api/v1/delivery-managers/** |
| order-service-route | lb://ORDER-SERVICE | /api/v1/drafts/**, /api/v1/orders/**, /api/v1/payments/** |
| company-service-route | lb://COMPANY-SERVICE | /api/v1/companies/**, /api/v1/categories/**, /api/v1/products/**, /api/v1/product-options/** |
| delivery-service-route | lb://DELIVERY-SERVICE | /api/v1/deliveries/**, /api/v1/delivery-addresses/**, /api/v1/internal/deliveries/** |
| hub-service-route | lb://HUB-SERVICE | /api/v1/hubs/**, /api/v1/hub-routes/**, /api/v1/warehouses/**, /api/v1/inventory/**, /api/v1/internal/inventory/**, /api/v1/internal/hub-routes/** |
| operations-service-route | lb://OPERATIONS-SERVICE | /api/v1/claims/**, /api/v1/slack/**, /api/v1/ai/** |

## 아키텍처
`config`(SecurityConfig)와 `filter`(JwtAuthenticationFilter, `GlobalFilter`+`Ordered` 구현) 두 패키지로만 구성된 얇은 게이트웨이 계층입니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 8080 (config-server의 api-gateway.yml 기준)
- 실행: `./gradlew :api-gateway:bootRun`
