# user-service

회원 인증/인가(Keycloak 연동)와 사용자, 배송담당자 정보를 관리하는 서비스입니다.

## 주요 기능
- 회원가입/로그인/토큰 재발급/로그아웃 (Keycloak Admin Client + OAuth2 Resource Server 연동)
- 사용자 조회/수정/삭제
- 관리자용 사용자 목록/승인 대기 목록 조회 및 승인 처리
- 배송담당자(DeliveryManager) 조회/배정/수정, 타 서비스가 호출하는 내부 배송담당자 정보 조회 API
- Redis(`RedisTemplate`)를 이용한 값 저장/조회 (`RedisService`)

## 기술 스택
- Spring Web, Spring Boot Actuator
- Spring Cloud Netflix Eureka Client, Spring Cloud Config Client
- Spring Data JPA, Spring Data Redis
- Spring Security, Spring Boot OAuth2 Resource Server, Keycloak Admin Client
- java-jwt(auth0), Spring Boot Validation
- springdoc-openapi-starter-webmvc-ui
- Micrometer Tracing(Brave) + Zipkin Reporter
- PostgreSQL

## API
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/auth/signup | 회원가입 |
| POST | /api/v1/auth/login | 로그인 |
| POST | /api/v1/auth/refresh | 토큰 재발급 |
| POST | /api/v1/auth/logout | 로그아웃 |
| GET | /api/v1/users/{userId} | 사용자 조회 |
| PATCH | /api/v1/users/{userId} | 사용자 수정 |
| DELETE | /api/v1/users/{userId} | 사용자 삭제 |
| GET | /api/v1/admin/users | (관리자) 사용자 목록 조회 |
| GET | /api/v1/admin/users/pending | (관리자) 승인 대기 사용자 조회 |
| PATCH | /api/v1/admin/users/{userId}/approval | (관리자) 사용자 승인 처리 |
| GET | /api/v1/delivery-managers/{userId} | 배송담당자 조회 |
| POST | /api/v1/delivery-managers/assign | 배송담당자 배정 |
| PATCH | /api/v1/delivery-managers/{userId} | 배송담당자 정보 수정 |
| POST | /api/v1/internal/users/manager-info | (내부) 배송담당자 정보 조회 |

## 아키텍처
`user`, `auth`, `admin`, `delivery`(배송담당자) 네 바운디드 컨텍스트가 각각 `application/infrastructure/presentation` 계층으로 분리되어 있고, Keycloak·Redis·Security 관련 설정은 `global/config` 하위에 모여 있습니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk`
- 기본 포트: 19091
- 실행: `./gradlew :user-service:bootRun`
