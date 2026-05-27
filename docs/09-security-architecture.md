# 09 - Security Architecture

> MSA 환경에서의 공통 보안 및 권한 제어 아키텍처

---

## 1. 개요

우리 서비스는 Gateway에서 1차 인증을 마친 후, 개별 서비스로 전달되는 헤더 정보를 기반으로 세밀한 권한 제어(RBAC 및 Ownership)를 수행합니다. 중복 코드를 방지하고 유지보수 효율을 높이기 위해 핵심 보안 로직을 `common` 모듈로 공통화하였습니다.

## 2. 주요 구성 요소

### 2.1 공통 모듈 (`common`)
모든 마이크로서비스가 공통으로 사용하는 보안 인프라입니다.

- **`CustomUserDetails`**: 게이트웨이가 전달한 `X-User-Id`, `X-User-Role`, `X-Company-Id`를 담는 인증 주체 객체입니다.
- **`CustomPreAuthFilter`**: HTTP 헤더를 파싱하여 `SecurityContext`에 인증 정보를 주입하는 서블릿 필터입니다.
    - 역할(Role) 앞에 자동으로 `ROLE_` 접두사를 붙여 스프링 시큐리티 표준(`hasRole`)과 호환되게 합니다.

### 2.2 서비스별 보안 설정 (`SecurityConfig`)
각 서비스는 `common`의 필터를 등록하여 보안을 활성화합니다.

- `@EnableMethodSecurity`: 메서드 수준 보안(`@PreAuthorize`)을 활성화합니다.
- `SecurityFilterChain`: Swagger 등 공개 경로를 제외한 모든 요청에 인증을 강제합니다.

### 2.3 소유권 검증 (`AuthService`)
단순 역할 체크를 넘어, 리소스의 실제 소유주인지 확인하는 비즈니스 보안 로직입니다.

- 각 서비스의 `global.application.service` 패키지에 위치합니다.
- `@PreAuthorize("@authService.isOwner(#id)")`와 같이 SpEL을 통해 호출됩니다.
- DB를 조회하여 현재 사용자의 `companyId`와 리소스의 소유 정보를 비교합니다.

## 3. 권한 제어 흐름 (Sequence)

1. **Gateway**: JWT 검증 후 `X-User-*` 헤더를 생성하여 서비스로 전달.
2. **Service Filter**: `CustomPreAuthFilter`가 헤더를 읽어 `CustomUserDetails` 생성 및 보관.
3. **Controller**: `@PreAuthorize` 어노테이션이 작동.
    - **Role Check**: `hasRole('MASTER')` 등 역할 확인.
    - **Ownership Check**: `AuthService`를 통해 DB 데이터와 대조.
4. **Business Logic**: 인증된 사용자 정보를 `@AuthenticationPrincipal`로 주입받아 사용.

## 4. 테스트 전략

- **AuthService Test**: Repository를 Mocking하여 소유권 판별 로직의 정확성을 검증합니다.
- **Controller Security Test**: 
    - `@WebMvcTest` 환경에서 다양한 헤더/역할 시나리오를 테스트합니다.
    - 권한 부족 시 `403 Forbidden`이 정상적으로 반환되는지 확인합니다.
- **Header Parsing Test**: 필터가 헤더 정보를 누락 없이 보안 컨텍스트에 담는지 확인합니다.
