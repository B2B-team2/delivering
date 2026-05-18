# 07 - Code Design

> 패키지 구조, 공통 모듈(global), 도메인별 주요 클래스 시그니처

---

## 1. 멀티 모듈 구조

```
delivering/                         (root project)
├── api-gateway/
├── config-server/
├── eureka-server/
├── user-service/
├── company-service/
├── hub-service/
├── order-service/
├── delivery-service/
├── operations-service/
└── build.gradle                    (공통 의존성 정의)
```

---

## 2. 서비스별 패키지 구조

각 서비스는 **Layered Architecture** 를 따른다.

```
{service-name}/
└── src/main/java/com/sparta/{servicename}/
    ├── {ServiceName}Application.java
    │
    ├── global/                         # 공통 모듈
    │   ├── config/                     # 설정 클래스 (Security, Feign, Redis 등)
    │   ├── exception/                  # 전역 예외 처리
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── CustomException.java
    │   │   └── ErrorCode.java
    │   ├── response/                   # 공통 응답 래퍼
    │   │   └── ApiResponse.java
    │   ├── audit/                      # JPA Auditing
    │   │   └── BaseEntity.java
    │   └── feign/                      # FeignClient 인터페이스
    │       ├── HubClient.java
    │       ├── UserClient.java
    │       └── ...
    │
    └── domain/                         # 도메인 레이어
        └── {domain}/
            ├── controller/
            │   └── {Domain}Controller.java
            ├── service/
            │   └── {Domain}Service.java
            ├── repository/
            │   └── {Domain}Repository.java
            ├── entity/
            │   └── {Domain}.java
            └── dto/
                ├── request/
                │   └── {Domain}Request.java
                └── response/
                    └── {Domain}Response.java
```

---

## 3. 공통 모듈 (global)

### 3.1 BaseEntity — JPA Audit 공통 엔티티

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;

    private LocalDateTime deletedAt;
    private String deletedBy;

    public void softDelete(String deletedBy) { ... }
    public boolean isDeleted() { ... }
}
```

> `p_delivery_log`는 append-only 이므로 `createdAt`, `createdBy`만 포함하는 별도 `BaseLogEntity` 사용

### 3.2 ApiResponse — 공통 응답 래퍼

```java
@Getter
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> created(T data) { ... }
    public static ApiResponse<Void> success() { ... }
}
```

### 3.3 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) { ... }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(...) { ... }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<?>> handleOptimisticLock(...) { ... }
}
```

### 3.4 ErrorCode — 에러 코드 Enum

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    APPROVAL_REQUIRED(403, "승인 대기 중인 계정입니다."),
    INSUFFICIENT_STOCK(409, "재고가 부족합니다."),
    OPTIMISTIC_LOCK_FAILURE(409, "재고 처리 중 충돌이 발생했습니다. 다시 시도해주세요."),
    DELIVERY_CANCEL_NOT_ALLOWED(400, "배송 시작 후에는 취소할 수 없습니다."),
    ...;

    private final int status;
    private final String message;
}
```

---

## 4. 도메인별 주요 클래스

### 4.1 User Service

```java
// Entity
@Entity @Table(name = "p_users", schema = "USER")
public class User extends BaseEntity {
    @Id @GeneratedValue private UUID userId;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String slackId;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;  // PENDING, APPROVED, REJECTED
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedReason;

    public void approve(UUID adminId) { ... }
    public void reject(String reason) { ... }
}

@Entity @Table(name = "p_delivery_managers", schema = "USER")
public class DeliveryManager extends BaseEntity {
    @Id private UUID userId;
    @Enumerated(EnumType.STRING)
    private ManagerType managerType;         // HUB_DELIVERY, COMPANY_DELIVERY
    private int deliveryOrder;
    private UUID hubId;
    private String status;
    private LocalDateTime lastAssignedAt;

    public void updateStatus(String status) { ... }
}

// Service
public class UserService {
    public UserResponse signup(SignupRequest request) { ... }
    public TokenResponse login(LoginRequest request) { ... }
    public void approveUser(UUID userId, UUID adminId) { ... }
    public void rejectUser(UUID userId, String reason) { ... }
    public DeliveryManagerResponse assignDeliveryManager(AssignRequest request) { ... }
}
```

### 4.2 Company Service

```java
@Entity @Table(name = "p_companies", schema = "COMPANY")
public class Company extends BaseEntity {
    @Id @GeneratedValue private UUID companyId;
    private String companyName;
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;         // PRODUCER, RECEIVER
    private String businessNumber;
    private UUID hubId;
    // PostGIS geometry 컬럼
    private Point latitude;
    private Point longitude;
}

@Entity @Table(name = "p_products", schema = "COMPANY")
public class Product extends BaseEntity {
    @Id @GeneratedValue private UUID productId;
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;            // ON_SALE, SOLD_OUT

    public void changeStatus(ProductStatus status) { ... }
}

@Entity @Table(name = "p_product_options", schema = "COMPANY")
public class ProductOption extends BaseEntity {
    @Id @GeneratedValue private UUID productOptionId;
    private UUID productId;
    private String optionsName;
    private BigDecimal extraPrice;
    private ProductStatus status;
    private int displayOrder;
}
```

### 4.3 Hub Service

```java
@Entity @Table(name = "p_warehouse_inventory", schema = "HUB")
public class WarehouseInventory extends BaseEntity {
    @Id @GeneratedValue private UUID inventoryId;
    private UUID warehouseId;
    private UUID productOptionId;
    private int quantity;
    private int reservedQuantity;
    private int safetyStock;
    @Version private long version;           // 낙관적 락

    public void reserve(int qty) { ... }     // reserved_quantity 증가
    public void cancelReservation(int qty) { ... }
    public void deduct(int qty) { ... }      // quantity, reserved_quantity 감소
    public int getAvailableQuantity() { return quantity - reservedQuantity; }
}

@Entity @Table(name = "p_inventory_histories", schema = "HUB")
public class InventoryHistory {              // append-only: created_at, created_by만 존재
    @Id @GeneratedValue private UUID historyId;
    private UUID inventoryId;
    private int changeQuantity;
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;           // INBOUND, OUTBOUND, RESERVED, CANCELLED, ADJUSTED, RETURNED
    private LocalDateTime createdAt;
    private String createdBy;
}

public class HubService {
    public void reserveStock(ReserveStockRequest request) { ... }
    public void cancelReservation(UUID inventoryId, int qty) { ... }
    public void deductStock(UUID inventoryId, int qty) { ... }
    public List<HubRouteDto> findRoute(UUID fromHub, UUID toHub) { ... }  // 경유 경로 탐색
}
```

### 4.4 Order Service

```java
@Entity @Table(name = "p_orders", schema = "ORDER")
public class Order extends BaseEntity {
    @Id @GeneratedValue private UUID orderId;
    private UUID requesterCompanyId;
    private UUID receiverCompanyId;
    private String recipientName;           // 스냅샷
    private String phone;
    private String slackId;
    @Type(JsonType.class)
    private String address;                 // JSON 스냅샷
    private LocalDateTime dueDate;
    private String requestMemo;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private BigDecimal finalPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;             // PENDING, DELIVERING, COMPLETED, CANCELLED

    public void cancel() { ... }
    public void complete() { ... }
}

public class OrderService {
    private final HubClient hubClient;
    private final DeliveryClient deliveryClient;
    private final OperationsClient operationsClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. 주문 생성
        // 2. 재고 예약 (hubClient)
        // 3. 배송 생성 (deliveryClient)
        // 4. AI 발송 시한 계산 (operationsClient)
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        // 1. 주문 취소
        // 2. 재고 복원 (hubClient)
    }
}
```

### 4.5 Delivery Service

```java
@Entity @Table(name = "p_deliveries", schema = "DELIVERY")
public class Delivery extends BaseEntity {
    @Id @GeneratedValue private UUID deliveryId;
    private UUID companyOrderId;
    private String trackingNumber;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;           // PENDING, SHIPPING, COMPLETED, CANCELLED
    private UUID departureHubId;
    private UUID destinationHubId;
    private String deliveryAddress;
    private String recipientName;
    private String recipientSlackId;
    private UUID deliveryManagerId;
    private LocalDateTime finalDispatchDeadlineAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public void start() { ... }
    public void complete() { ... }
    public void cancel() { ... }            // PENDING만 허용
    public void reassignManager(UUID newManagerId) { ... }
}

@Entity @Table(name = "p_delivery_routes", schema = "DELIVERY")
public class DeliveryRoute extends BaseEntity {
    @Id @GeneratedValue private UUID routeId;
    private UUID deliveryId;
    private int sequence;
    private UUID fromHubId;
    private UUID toHubId;
    private BigDecimal estimatedDistance;
    private LocalDateTime estimatedDuration;
    private BigDecimal actualDistance;
    private LocalDateTime actualDuration;
    @Enumerated(EnumType.STRING)
    private RouteStatus status;              // PENDING, MOVING, ARRIVED, CANCELLED

    public void startMoving() { ... }
    public void arrive(BigDecimal actualDist, LocalDateTime actualDur) { ... }
}

@Entity @Table(name = "p_delivery_log", schema = "DELIVERY")
public class DeliveryLog {                  // append-only
    @Id @GeneratedValue private UUID logId;
    private UUID deliveryId;
    private UUID routeId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;            // MANAGER_ASSIGNED, MANAGER_CHANGED, STATUS_CHANGED, ROUTE_CHANGED, CANCELLED
    @Type(JsonType.class)
    private String previousValue;
    @Type(JsonType.class)
    private String currentValue;
    private String reason;
    private LocalDateTime createdAt;
    private String createdBy;
}
```

### 4.6 Operations Service

```java
@Entity @Table(name = "p_order_claims", schema = "OPS")
public class OrderClaim extends BaseEntity {
    @Id @GeneratedValue private UUID claimId;
    private UUID orderItemId;
    @Enumerated(EnumType.STRING)
    private ClaimType claimType;             // RETURN, EXCHANGE
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;              // REQUESTED, PROCESSING, REJECTED, COMPLETED, CANCELLED
    private String reason;
    private BigDecimal refundAmount;

    public void startProcessing() { ... }
    public void complete(BigDecimal refundAmount) { ... }
    public void reject() { ... }
}

@Entity @Table(name = "p_ai_requests", schema = "OPS")
public class AiRequest extends BaseEntity {
    @Id @GeneratedValue private UUID requestId;
    private UUID userId;
    private UUID deliveryId;
    private String aiModelName;
    private String promptText;
    private String responseText;
    @Enumerated(EnumType.STRING)
    private AiRequestStatus status;          // PENDING, SUCCESS, FAILED
    private String errorMessage;
    private LocalDateTime finalDeadlineAt;
}

public class AiService {
    public AiDeadlineResponse predictDeadline(PredictDeadlineRequest request) {
        // 1. 프롬프트 구성 (상품/수량, 납기, 경로, 근무시간 09~18시)
        // 2. Gemini API 호출 (Spring AI)
        // 3. final_deadline_at 파싱 및 저장
        // 4. 슬랙 발송
    }
}
```

---

## 5. FeignClient 인터페이스

```java
// order-service → hub-service
@FeignClient(name = "hub-service")
public interface HubClient {
    @PostMapping("/internal/inventory/reserve")
    void reserveStock(@RequestBody ReserveStockRequest request);

    @PostMapping("/internal/inventory/cancel")
    void cancelReservation(@RequestBody CancelReservationRequest request);

    @PostMapping("/internal/hub-routes/search")
    List<HubRouteDto> searchRoute(@RequestBody RouteSearchRequest request);
}

// order-service → delivery-service
@FeignClient(name = "delivery-service")
public interface DeliveryClient {
    @PostMapping("/internal/deliveries")
    DeliveryResponse createDelivery(@RequestBody CreateDeliveryRequest request);
}

// delivery-service → user-service
@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/internal/delivery-managers/assign")
    DeliveryManagerResponse assignManager(@RequestBody AssignManagerRequest request);
}

// delivery-service → order-service
@FeignClient(name = "order-service")
public interface OrderClient {
    @PatchMapping("/internal/orders/{orderId}/complete")
    void completeOrder(@PathVariable UUID orderId);
}
```

---

## 6. 주요 설정 클래스

### SecurityConfig (api-gateway)
- JWT 검증 필터 적용
- 화이트리스트: `/auth/signup`, `/auth/login`

### RedisConfig (hub-service)
- `@EnableCaching`
- `RedisCacheManager` 설정 (허브·경로 TTL 설정)

### JpaConfig (각 서비스)
- `@EnableJpaAuditing`
- `AuditorAware` 구현으로 `createdBy`, `updatedBy` 자동 주입

### FeignConfig (공통)
- 재시도 로직: `Retryer.Default(100, 1000, 3)` (최대 3회)
- 에러 디코더: FeignClient 오류 → `CustomException` 변환
