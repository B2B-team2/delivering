package com.sparta.hubservice.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hubservice.global.client.CompanyClient;
import com.sparta.hubservice.global.client.OrderClient;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import com.sparta.hubservice.inventory.infrastructure.repository.InventoryHistoryJpaRepository;
import com.sparta.hubservice.inventory.infrastructure.repository.WarehouseInventoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * InternalInventoryController 통합 테스트
 *
 * - H2 인메모리 DB (test/resources/application.yml)
 * - @Transactional: 각 테스트 종료 후 자동 롤백 → 테스트 간 DB 격리
 * - MockMvc가 동일 스레드에서 디스패치되므로 서비스 트랜잭션이 테스트 트랜잭션에 합류함
 *   → @BeforeEach 저장 재고를 서비스가 즉시 조회 가능
 *   → 서비스 호출 후 레포지토리로 DB 상태 직접 검증 가능
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class InternalInventoryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private InventoryService inventoryService;
    @Autowired private WarehouseInventoryJpaRepository inventoryRepository;
    @Autowired private InventoryHistoryJpaRepository historyRepository;

    @MockBean private CompanyClient companyClient;
    @MockBean private OrderClient orderClient;

    private UUID productOptionId;
    private UUID inventoryId;

    @BeforeEach
    void setUp() {
        productOptionId = UUID.randomUUID();
        WarehouseInventory inv = inventoryRepository.save(
            WarehouseInventory.builder()
                .warehouseId(UUID.randomUUID())
                .productOptionId(productOptionId)
                .quantity(100)
                .safetyStock(10)
                .build()
        );
        inventoryId = inv.getInventoryId();
    }

    // ─── 요청 바디 헬퍼 ────────────────────────────────────────────────────────

    private Map<String, Object> reserveBody(UUID orderId, UUID companyOrderId, int qty) {
        return Map.of(
            "orderId", orderId.toString(),
            "companyOrderId", companyOrderId.toString(),
            "items", List.of(Map.of(
                "productOptionId", productOptionId.toString(),
                "quantity", qty
            ))
        );
    }

    private Map<String, Object> bulkBody(UUID orderId, int qty) {
        return Map.of(
            "orderId", orderId.toString(),
            "items", List.of(Map.of(
                "productOptionId", productOptionId.toString(),
                "quantity", qty
            ))
        );
    }

    // ─── POST /api/v1/internal/inventory/reserve ──────────────────────────────

    @Test
    void 재고_예약_성공_reservedQuantity_증가_이력_저장() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 30))))
            .andExpect(status().isOk());

        WarehouseInventory result = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(result.getReservedQuantity()).isEqualTo(30);
        assertThat(result.getAvailableQuantity()).isEqualTo(70);

        var histories = historyRepository.findByInventoryId(inventoryId);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getChangeType()).isEqualTo(InventoryChangeType.RESERVED);
        assertThat(histories.get(0).getOrderId()).isEqualTo(orderId);
        assertThat(histories.get(0).getCompanyOrderId()).isEqualTo(companyOrderId);
        assertThat(histories.get(0).getChangeQuantity()).isEqualTo(-30); // 재고 감소 → 음수
    }

    @Test
    void 재고_예약_재고_부족_409_DB_불변() throws Exception {
        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    reserveBody(UUID.randomUUID(), UUID.randomUUID(), 200))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("재고가 부족합니다."));

        WarehouseInventory unchanged = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(unchanged.getReservedQuantity()).isEqualTo(0);
        assertThat(unchanged.getAvailableQuantity()).isEqualTo(100);
        assertThat(historyRepository.findByInventoryId(inventoryId)).isEmpty();
    }

    @Test
    void 없는_productOptionId_예약_404() throws Exception {
        Map<String, Object> body = Map.of(
            "orderId", UUID.randomUUID().toString(),
            "companyOrderId", UUID.randomUUID().toString(),
            "items", List.of(Map.of(
                "productOptionId", UUID.randomUUID().toString(),
                "quantity", 10
            ))
        );

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNotFound());
    }

    // ─── POST /api/v1/internal/inventory/cancel ───────────────────────────────

    @Test
    void 예약_전체_취소_reservedQuantity_복원_CANCELLED_이력_저장() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 40))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/internal/inventory/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("orderId", orderId.toString()))))
            .andExpect(status().isOk());

        WarehouseInventory restored = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(restored.getReservedQuantity()).isEqualTo(0);
        assertThat(restored.getAvailableQuantity()).isEqualTo(100);

        var histories = historyRepository.findByInventoryId(inventoryId);
        assertThat(histories).hasSize(2); // RESERVED + CANCELLED

        var reserved = histories.stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.RESERVED).findFirst().orElseThrow();
        assertThat(reserved.getChangeQuantity()).isEqualTo(-40); // 재고 감소 → 음수

        var cancelled = histories.stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.CANCELLED).findFirst().orElseThrow();
        assertThat(cancelled.getChangeQuantity()).isEqualTo(40); // 재고 복원 → 양수
    }

    @Test
    void 예약_없는_orderId_전체_취소_404() throws Exception {
        mockMvc.perform(post("/api/v1/internal/inventory/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("orderId", UUID.randomUUID().toString()))))
            .andExpect(status().isNotFound());
    }

    // ─── POST /api/v1/internal/inventory/cancel/company ──────────────────────

    @Test
    void 예약_업체별_취소_reservedQuantity_복원() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 25))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/internal/inventory/cancel/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("companyOrderId", companyOrderId.toString()))))
            .andExpect(status().isOk());

        WarehouseInventory restored = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(restored.getReservedQuantity()).isEqualTo(0);
        assertThat(restored.getAvailableQuantity()).isEqualTo(100);

        var histories = historyRepository.findByInventoryId(inventoryId);
        assertThat(histories).hasSize(2); // RESERVED + CANCELLED

        var reservedHistory = histories.stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.RESERVED).findFirst().orElseThrow();
        assertThat(reservedHistory.getChangeQuantity()).isEqualTo(-25); // 예약 = 음수

        var cancelledHistory = histories.stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.CANCELLED).findFirst().orElseThrow();
        assertThat(cancelledHistory.getChangeQuantity()).isEqualTo(25); // 취소 복원 = 양수
    }

    // ─── POST /api/v1/internal/inventory/deduct ───────────────────────────────

    @Test
    void 재고_차감_quantity_감소_reservedQuantity_해제_OUTBOUND_이력() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 20))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/internal/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBody(orderId, 20))))
            .andExpect(status().isOk());

        WarehouseInventory deducted = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(deducted.getQuantity()).isEqualTo(80);
        assertThat(deducted.getReservedQuantity()).isEqualTo(0);
        assertThat(deducted.getAvailableQuantity()).isEqualTo(80);

        assertThat(historyRepository.findByInventoryId(inventoryId).stream()
            .anyMatch(h -> h.getChangeType() == InventoryChangeType.OUTBOUND)).isTrue();
    }

    @Test
    void 재고_차감_수량_초과_409_DB_불변() throws Exception {
        mockMvc.perform(post("/api/v1/internal/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBody(UUID.randomUUID(), 200))))
            .andExpect(status().isConflict());

        WarehouseInventory unchanged = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(100);
    }

    // ─── POST /api/v1/internal/inventory/return ───────────────────────────────

    @Test
    void 반품_재고_복원_quantity_증가_RETURNED_이력() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBody(orderId, 15))))
            .andExpect(status().isOk());

        WarehouseInventory returned = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(returned.getQuantity()).isEqualTo(115);

        var histories = historyRepository.findByInventoryId(inventoryId);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getChangeType()).isEqualTo(InventoryChangeType.RETURNED);
        assertThat(histories.get(0).getOrderId()).isEqualTo(orderId);
        assertThat(histories.get(0).getChangeQuantity()).isEqualTo(15); // 재고 복원 → 양수
    }

    // ─── 이력 changeQuantity 부호 검증 ────────────────────────────────────────

    /**
     * 모든 재고 변동 작업의 changeQuantity 부호 정책 검증
     *
     * 음수 = 재고 감소 (RESERVED, OUTBOUND)
     * 양수 = 재고 증가 (CANCELLED, RETURNED)
     *
     * 요청 body의 quantity는 항상 양수 — 부호는 서비스 내부에서 결정
     */
    @Test
    void 전체_흐름_재고_수치와_이력_changeQuantity_부호_일치_검증() throws Exception {
        // 초기: quantity=100, reserved=0, available=100
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        // 1. 예약 (qty=30) → reservedQuantity +30, changeQuantity=-30
        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 30))))
            .andExpect(status().isOk());

        WarehouseInventory afterReserve = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(afterReserve.getQuantity()).isEqualTo(100);
        assertThat(afterReserve.getReservedQuantity()).isEqualTo(30);
        assertThat(afterReserve.getAvailableQuantity()).isEqualTo(70);

        var afterReserveHistories = historyRepository.findByInventoryId(inventoryId);
        assertThat(afterReserveHistories).hasSize(1);
        assertThat(afterReserveHistories.get(0).getChangeQuantity()).isEqualTo(-30);

        // 2. 취소 → reservedQuantity -30, changeQuantity=+30
        mockMvc.perform(post("/api/v1/internal/inventory/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("orderId", orderId.toString()))))
            .andExpect(status().isOk());

        WarehouseInventory afterCancel = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(afterCancel.getQuantity()).isEqualTo(100);
        assertThat(afterCancel.getReservedQuantity()).isEqualTo(0);
        assertThat(afterCancel.getAvailableQuantity()).isEqualTo(100);

        var afterCancelHistories = historyRepository.findByInventoryId(inventoryId);
        assertThat(afterCancelHistories).hasSize(2);
        var cancelledHistory = afterCancelHistories.stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.CANCELLED).findFirst().orElseThrow();
        assertThat(cancelledHistory.getChangeQuantity()).isEqualTo(30);

        // 3. 예약 후 차감 (qty=20) → quantity -20, reservedQuantity -20, changeQuantity=-20
        UUID orderId2 = UUID.randomUUID();
        UUID companyOrderId2 = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId2, companyOrderId2, 20))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/internal/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBody(orderId2, 20))))
            .andExpect(status().isOk());

        WarehouseInventory afterDeduct = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(afterDeduct.getQuantity()).isEqualTo(80);
        assertThat(afterDeduct.getReservedQuantity()).isEqualTo(0);
        assertThat(afterDeduct.getAvailableQuantity()).isEqualTo(80);

        var outboundHistory = historyRepository.findByInventoryId(inventoryId).stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.OUTBOUND).findFirst().orElseThrow();
        assertThat(outboundHistory.getChangeQuantity()).isEqualTo(-20);

        // 4. 반품 (qty=10) → quantity +10, changeQuantity=+10
        mockMvc.perform(post("/api/v1/internal/inventory/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBody(orderId2, 10))))
            .andExpect(status().isOk());

        WarehouseInventory afterReturn = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(afterReturn.getQuantity()).isEqualTo(90);
        assertThat(afterReturn.getReservedQuantity()).isEqualTo(0);

        var returnedHistory = historyRepository.findByInventoryId(inventoryId).stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.RETURNED).findFirst().orElseThrow();
        assertThat(returnedHistory.getChangeQuantity()).isEqualTo(10);
    }

    // ─── deductStockByCompanyOrder / revertDeductByCompanyOrder ──────────────

    @Test
    void 출고_재고차감_quantity_감소_reservedQuantity_감소_OUTBOUND_이력() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        // 예약
        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 30))))
            .andExpect(status().isOk());

        // 출고 차감 (companyOrderId 기반 서비스 직접 호출)
        inventoryService.deductStockByCompanyOrder(companyOrderId);

        WarehouseInventory result = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(result.getQuantity()).isEqualTo(70);
        assertThat(result.getReservedQuantity()).isEqualTo(0);
        assertThat(result.getAvailableQuantity()).isEqualTo(70);

        var outbound = historyRepository.findByInventoryId(inventoryId).stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.OUTBOUND).findFirst().orElseThrow();
        assertThat(outbound.getChangeQuantity()).isEqualTo(-30);
        assertThat(outbound.getCompanyOrderId()).isEqualTo(companyOrderId);
    }

    @Test
    void 출고_차감_후_보상_재고복원_quantity_reservedQuantity_원복() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID companyOrderId = UUID.randomUUID();

        // 예약
        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveBody(orderId, companyOrderId, 20))))
            .andExpect(status().isOk());

        // 출고 차감
        inventoryService.deductStockByCompanyOrder(companyOrderId);

        // 보상: 차감 복원
        inventoryService.revertDeductByCompanyOrder(companyOrderId);

        // 예약 상태로 복원됨
        WarehouseInventory reverted = inventoryRepository
            .findByInventoryIdAndDeletedAtIsNull(inventoryId).orElseThrow();
        assertThat(reverted.getQuantity()).isEqualTo(100);
        assertThat(reverted.getReservedQuantity()).isEqualTo(20);
        assertThat(reverted.getAvailableQuantity()).isEqualTo(80);

        var cancelled = historyRepository.findByInventoryId(inventoryId).stream()
            .filter(h -> h.getChangeType() == InventoryChangeType.CANCELLED).findFirst().orElseThrow();
        assertThat(cancelled.getChangeQuantity()).isEqualTo(20);
    }

    // ─── 유효성 검증 ──────────────────────────────────────────────────────────

    @Test
    void 예약_orderId_누락_400() throws Exception {
        Map<String, Object> body = Map.of(
            "companyOrderId", UUID.randomUUID().toString(),
            "items", List.of(Map.of(
                "productOptionId", productOptionId.toString(),
                "quantity", 10
            ))
        );

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_items_빈_리스트_400() throws Exception {
        Map<String, Object> body = Map.of(
            "orderId", UUID.randomUUID().toString(),
            "items", List.of()
        );

        mockMvc.perform(post("/api/v1/internal/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }
}
