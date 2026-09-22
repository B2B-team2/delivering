# eureka-server

sparta-logistics 마이크로서비스들이 서로를 찾을 수 있도록 하는 Spring Cloud Netflix Eureka 서비스 디스커버리 서버입니다.

## 주요 기능
- 각 서비스(api-gateway 포함)가 자신을 등록(register)하고, api-gateway/Feign 클라이언트가 `lb://SERVICE-NAME` 형태로 다른 서비스를 조회할 수 있게 함
- 자기 자신은 다른 Eureka에 등록하지 않음(`register-with-eureka: false`, `fetch-registry: false`) — 단일 디스커버리 서버로 구성
- 상태 확인 경로를 `/actuator/health`로 지정 (`eureka.instance.status-page-url-path`)

## 기술 스택
- Spring Cloud Netflix Eureka Server

## 아키텍처
별도 도메인 패키지 없이 Spring Boot 애플리케이션 하나로 구성된 얇은 인프라 서버입니다.

## 실행
- Dockerfile 베이스 이미지: `eclipse-temurin:17-jdk` (curl 설치 포함)
- 기본 포트: 8761
- 실행: `./gradlew :eureka-server:bootRun`
