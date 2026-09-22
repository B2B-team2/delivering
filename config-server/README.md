# config-server

sparta-logistics의 모든 마이크로서비스가 사용하는 설정 값을 중앙에서 관리하는 Spring Cloud Config Server입니다.

## 주요 기능
- `native` 프로파일로 `classpath:/config`(`config-server/src/main/resources/config/*.yml`) 아래의 서비스별 설정 파일을 서빙
- 각 서비스(api-gateway, company-service, delivery-service, hub-service, operations-service, order-service, user-service)는 `spring.application.name` 기준으로 자신의 설정을 조회
- api-gateway의 라우팅 규칙(spring.cloud.gateway.routes)도 이 서버가 서빙하는 `api-gateway.yml`에 정의됨

## 기술 스택
- Spring Cloud Config Server

## 아키텍처
별도 도메인 패키지 없이 Spring Boot 애플리케이션 + `src/main/resources/config/` 설정 파일 저장소로만 구성된 얇은 인프라 서버입니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk` (curl 설치 포함)
- 기본 포트: 8888
- 실행: `./gradlew :config-server:bootRun`
