# 07 - Code Design

> 멀티 모듈 구조, common 모듈, 서비스별 패키지 구조, 도메인별 핵심 엔티티, FeignClient 인터페이스, 주요 설정, ArchUnit 아키텍처 테스트, Checkstyle 코드 컨벤션

---

## 1. 멀티 모듈 구조

```
delivering/                         (root project)
├── common/                         (공유 라이브러리 모듈)
├── api-gateway/
├── config-server/ㄴ
├── eureka-server/
├── user-service/
├── company-service/
├── hub-service/
├── order-service/
├── delivery-service/
├── operations-service/
├── settings.gradle
└── build.gradle                    (공통 의존성 정의)
```

### common 모듈 (`com.sparta.common`)

모든 마이크로서비스가 공통으로 의존하는 공유 라이브러리.

```
common/src/main/java/com/sparta/common/
├── dto/
│   ├── ApiResponse.java         (공통 응답 래퍼)
│   ├── PageResponse.java        (페이지네이션 응답)
│   ├── BusinessException.java   (비즈니스 예외)
│   ├── ErrorCode.java           (에러 코드 인터페이스)
│   ├── CommonErrorCode.java     (공통 에러 코드 Enum)
│   └── ErrorResponse.java
├── entity/
│   └── BaseEntity.java          (JPA Auditing 공통 엔티티)
├── handler/
│   └── GlobalExceptionHandler.java
├── security/
│   ├── AuditorAwareImpl.java    (createdBy, updatedBy 자동 주입)
│   ├── CustomPreAuthFilter.java (Gateway → 서비스 헤더 인증)
│   ├── CustomUserDetails.java
│   └── SecurityUtil.java
├── util/
│   └── PageableUtil.java
└── architecture/
    └── BaseArchitectureTest.java (ArchUnit 아키텍처 테스트 베이스)
```

---

## 2. 서비스별 패키지 구조

각 서비스는 **4계층 Layered Architecture** 를 따른다.

```
{service-name}/
└── src/main/java/com/sparta/{servicename}/
    ├── {ServiceName}Application.java
    │
    ├── global/                             # 서비스별 설정 (공통 로직은 common 모듈)
    │   ├── config/                         # Security, Feign, Redis 등 설정 클래스
    │   ├── exception/                      # 서비스 전용 ErrorCode enum
    │   │   └── {Domain}ErrorCode.java
    │   ├── port/                           # 외부 서비스 호출 추상화 인터페이스 (필요 시)
    │   │   └── {Target}Port.java
    │   └── security/                       # SecurityUtil 등 서비스별 인증 유틸
    │
    └── {domain}/                           # 도메인 패키지
        │
        ├── presentation/                   # 1계층: 외부 요청/응답
        │   ├── controller/
        │   │   └── {Domain}Controller.java
        │   └── dto/
        │       ├── request/
        │       └── response/
        │
        ├── application/                    # 2계층: 비즈니스 로직
        │   ├── service/
        │   │   └── {Domain}Service.java
        │   ├── port/                       # 외부 서비스 포트 인터페이스 (필요 시)
        │   └── dto/                        # Command / Result DTO
        │
        ├── domain/                         # 3계층: 핵심 도메인
        │   ├── core/
        │   │   ├── {Domain}.java           # JPA Entity
        │   │   └── {Domain}Status.java     # 상태 Enum
        │   └── repository/                 # 순수 Java Repository 인터페이스 (선택)
        │       └── {Domain}Repository.java
        │
        └── infrastructure/                 # 4계층: 외부 연동
            ├── repository/
            │   ├── {Domain}JpaRepository.java    # Spring Data JPA 인터페이스
            │   └── {Domain}RepositoryImpl.java   # Repository 구현체 (선택)
            └── client/                     # FeignClient
                ├── {Target}Client.java
                └── dto/
```

---

## 3. 공통 모듈 (common)

> 공통 클래스는 각 서비스의 `global/` 패키지가 아닌 루트의 **`common` 모듈**에 위치한다.  
> 각 서비스는 `implementation project(':common')`으로 의존.

### 3.1 BaseEntity — JPA Audit 공통 엔티티 (`com.sparta.common.entity`)

```
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate private LocalDateTime createdAt;
    @CreatedBy   private UUID createdBy;
    @LastModifiedDate private LocalDateTime updatedAt;
    @LastModifiedBy   private UUID updatedBy;

    private LocalDateTime deletedAt;
    private UUID deletedBy;

    public void softDelete(UUID deletedBy) { }
    public void clearDeleted() { }    // 소프트 삭제 복원
    public boolean isDeleted() { return deletedAt != null; }
}
```

> `p_inventory_histories`는 append-only이므로 BaseEntity 미상속, `@CreatedDate`/`@CreatedBy`만 직접 선언.

### 3.2 ApiResponse — 공통 응답 래퍼 (`com.sparta.common.dto`)

```
@Getter
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(200, "SUCCESS", data); }
    public static <T> ApiResponse<T> created(T data) { return new ApiResponse<>(201, "SUCCESS", data); }
    public static ApiResponse<Void> success() { return new ApiResponse<>(200, "SUCCESS", null); }
}
```

### 3.3 GlobalExceptionHandler (`com.sparta.common.handler`)

```
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) { }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException e) { }
}
```

### 3.4 BusinessException / ErrorCode (`com.sparta.common.dto`)

```
// 서비스별 ErrorCode Enum이 이 인터페이스를 구현
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}

// 런타임 예외
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    public BusinessException(ErrorCode errorCode) { super(errorCode.getMessage()); }
}
```

### 3.5 CustomPreAuthFilter — 게이트웨이 인증 헤더 처리

API Gateway가 Keycloak JWT를 검증한 뒤 사용자 정보를 헤더로 전달한다.

```
X-User-Id   : UUID (사용자 ID)
X-User-Role : String (역할 코드)
```

각 서비스는 `CustomPreAuthFilter`가 해당 헤더를 파싱해 `SecurityContext`에 `CustomUserDetails`를 등록한다.  
서비스 내부에서는 `SecurityUtil.getCurrentUserId()` 등으로 사용한다.

---


## 4. 주요 설정 클래스

### SecurityConfig (api-gateway)
- Keycloak에서 발급된 JWT 검증 (`NimbusJwtDecoder`)
- `anyExchange().permitAll()` — 인가는 각 서비스 레이어에서 처리
- Gateway는 JWT 검증 후 `X-User-Id`, `X-User-Role` 헤더를 하위 서비스로 전달

### CustomPreAuthFilter (common — 각 서비스 공통 적용)
- 헤더 `X-User-Id`, `X-User-Role` 파싱 → `CustomUserDetails` → `SecurityContext` 등록
- 서비스 내부: `SecurityUtil.getCurrentUserId()`, `SecurityUtil.getCurrentUserRole()` 로 사용

### RedisConfig (hub-service)
- `@EnableCaching`, `RedisCacheManager`
- 허브·경로 캐시 TTL 설정, 수정·삭제 시 `CacheEvict`

### JpaConfig (각 서비스)
- `@EnableJpaAuditing`
- `AuditorAwareImpl` — `SecurityContext`에서 UUID 추출 → `createdBy`, `updatedBy` 자동 주입

### FeignConfig (각 서비스)
- `Retryer.Default(100, 1000, 3)` — 최대 3회 재시도
- `GatewayFeignInterceptor` — FeignClient 호출 시 인증 헤더 자동 전달

---

## 5. 아키텍처 테스트 (ArchUnit)

`common` 모듈의 `BaseArchitectureTest`를 각 서비스가 상속해 아키텍처 규칙을 자동 검증한다.

### 적용 서비스
`company-service`, `hub-service`, `operations-service`, `order-service`

```java
@AnalyzeClasses(packages = "com.sparta.orderservice", importOptions = ImportOption.DoNotIncludeTests.class)
public class OrderArchitectureTest extends BaseArchitectureTest {
    @ArchTest
    static final ArchRule order_prefix_rule = domain_prefix_naming_rule("Order");
    @ArchTest
    static final ArchRule payment_prefix_rule = domain_prefix_naming_rule("Payment");
    @ArchTest
    static final ArchRule draft_prefix_rule = domain_prefix_naming_rule("Draft");
}
```

### 검증 규칙

| 규칙 | 내용 |
|---|---|
| **Presentation 위치** | `Controller`로 끝나는 클래스는 `..presentation.controller..` 패키지에 위치 |
| **Presentation 어노테이션** | `..presentation.controller..` 클래스는 `@RestController` 필수, `@Controller` 금지 |
| **Application 위치** | `Service`로 끝나는 클래스는 `..application.service..` 패키지에 위치 |
| **Application 어노테이션** | `Service`로 끝나는 클래스는 `@Service` 필수 |
| **Domain Entity 위치** | `@Entity` 클래스는 `..domain.core..` 패키지에 위치 |
| **Domain Enum 위치** | `..domain..` 패키지의 Enum은 `..domain.core..`에 위치 |
| **Domain Repository 위치** | `Repository`로 끝나는 순수 인터페이스(Jpa 제외)는 `..domain.repository..`에 위치 |
| **Infra JpaRepository 위치** | `JpaRepository`로 끝나는 인터페이스는 `..infrastructure..`에 위치 |
| **Infra RepositoryImpl 위치** | `RepositoryImpl`로 끝나는 클래스는 `..infrastructure..`에 위치 |
| **@Transactional 금지** | `..presentation..` 패키지에서 `@Transactional` 사용 금지 |
| **4계층 의존성** | Presentation → Application → Domain ← Infrastructure (역방향 금지) |
| **도메인 Prefix** | 각 도메인 클래스명에 도메인 키워드 포함 필수 (e.g. `OrderController`, `OrderService`) |

### 의존성 규칙 상세

```
Presentation  → Application (가능)
Application   → Domain      (가능)
Infrastructure → Domain     (가능, DIP)
Infrastructure → Application (가능, DIP 구현체)

Presentation  → Domain      (금지 — DTO 경유)
Presentation  → Infrastructure (금지)
Application   → Infrastructure (금지 — 인터페이스 경유)
```

> `ALLOW_EMPTY = false` 설정으로 조건에 매칭되는 클래스가 없을 때도 테스트 실패 처리.

---

## 6. 코드 컨벤션 (Checkstyle)

루트 `build.gradle`에 전 서비스 공통으로 Checkstyle이 적용된다.

### 설정 (`config/checkstyle/checkstyle.xml`)

| 규칙 | 내용 |
|---|---|
| **AvoidStarImport** | 와일드카드 import 금지 (`import java.util.*`, `import static ....*`) |

### 빌드 설정

```groovy
checkstyle {
    maxWarnings = 0           // 경고 0개 초과 시 빌드 실패
    ignoreFailures = false    // 위반 시 빌드 중단
    toolVersion = "10.12.5"
}

// 모든 JavaCompile 전에 checkstyleMain 강제 실행
tasks.withType(JavaCompile) {
    dependsOn 'checkstyleMain'
}
```

> 위반 시 HTML 리포트 생성 (`build/reports/checkstyle/`), 콘솔에 위반 내용 출력.
