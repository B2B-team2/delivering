# Claim 통합 테스트 시나리오

이 문서는 `operations-service`의 클레임(Claim) 도메인에서 수행되는 SAGA 패턴 및 통합 테스트 흐름을 정의합니다.

## 🛠️ 공통 전제 조건
- **환경**: `IntegrationTestSupport` 상속 기반 통합 테스트.
- **외부 서비스 모킹**: `OrderPort` (주문 서비스), `HubPort` (허브/재고 서비스)와의 통신은 `@MockBean`을 사용하여 성공/실패 시나리오를 시뮬레이션함.
- **인증/인가**: `X-User-Id`, `X-User-Role` 헤더를 사용하여 관리자(MASTER) 및 업체 담당자(COMPANY_MANAGER) 권한을 구분함.

---

## 📋 시나리오 리스트

### 1. SAGA 정상 흐름: 클레임 승인 및 재고 복원
클레임 요청이 들어왔을 때 외부 서비스(주문, 재고)와 연동하여 정상적으로 처리가 완료되는 전체 흐름을 검증합니다.

- **Step 1: 클레임 생성**
    - **행동**: `COMPANY_MANAGER` 권한으로 `POST /api/v1/claims` 호출. (주문 ID, 사유 등 포함)
    - **기대 결과**: `201 Created` 반환 및 클레임 상태가 `REQUESTED`로 저장됨.
- **Step 2: 외부 연동 설정 (Mocking)**
    - **설정**: 주문 서비스에서 해당 주문의 품목 정보를 정상적으로 반환하도록 설정.
- **Step 3: 클레임 상태 변경 (승인 시작)**
    - **행동**: `MASTER` 권한으로 `PATCH /api/v1/claims/{claimId}/status` 호출 (상태를 `PROCESSING`으로 변경).
    - **기대 결과**: `200 OK` 반환.
- **Step 4: 최종 검증**
    - **검증**: 클레임 엔티티의 상태가 최종적으로 `PROCESSING`으로 변경되었는지 데이터베이스 확인.

### 2. SAGA 보상 트랜잭션: 주문 서비스 연동 실패 시 롤백
클레임 처리 중 특정 단계(주문 취소)에서 실패했을 때, 이미 수행된 작업(재고 복원)이 다시 취소(재차감)되는지 검증합니다.

- **Step 1: 클레임 생성**
    - **행동**: 클레임 생성 요청 (`REQUESTED` 상태).
- **Step 2: 실패 상황 설정 (Mocking)**
    - **설정**: 주문 서비스(OrderPort) 호출 시 의도적으로 예외(`RuntimeException`)를 발생시킴.
- **Step 3: 클레임 상태 변경 요청**
    - **행동**: `MASTER` 권한으로 상태를 `PROCESSING`으로 변경 요청.
- **Step 4: 보상 트랜잭션 및 결과 검증**
    - **기대 결과**: API 응답은 실패(`500 Internal Server Error`)를 반환해야 함.
    - **검증**: 
        1. `HubPort.returnStock()`(재고 복원)이 호출된 후, 실패에 따라 다시 `HubPort.deductStock()`(재고 재차감)이 호출되었는지 확인.
        2. 클레임 엔티티의 상태가 `PROCESSING`으로 변하지 않고 초기 상태인 `REQUESTED`로 유지되는지 확인.

---

## 📝 향후 추가 예정 시나리오
- 클레임 거절(REJECTED) 시의 흐름 및 알림 연동 검증.
- 환불 금액(Refund Amount) 계산 로직 정합성 검증.
