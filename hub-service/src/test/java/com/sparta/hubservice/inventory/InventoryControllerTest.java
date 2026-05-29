package com.sparta.hubservice.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.config.SecurityConfig;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustDto;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryDto;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.application.service.InventoryShipmentService;
import com.sparta.hubservice.inventory.presentation.controller.InventoryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private InventoryService inventoryService;
    @MockBean  private InventoryShipmentService shipmentService;

    private static final UUID USER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private WarehouseInventoryDto inventoryFixture(UUID inventoryId, UUID warehouseId) {
        return WarehouseInventoryDto.builder()
                .inventoryId(inventoryId)
                .warehouseId(warehouseId)
                .productOptionId(UUID.randomUUID())
                .quantity(100)
                .reservedQuantity(0)
                .availableQuantity(100)
                .safetyStock(10)
                .createdAt(LocalDateTime.of(2026, 5, 26, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 5, 26, 10, 0))
                .build();
    }

    private WarehouseInventoryAdjustDto adjustFixture(UUID inventoryId) {
        return WarehouseInventoryAdjustDto.builder()
                .inventoryId(inventoryId)
                .productOptionId(UUID.randomUUID())
                .previousQuantity(100)
                .changeQuantity(30)
                .currentQuantity(130)
                .reservedQuantity(0)
                .availableQuantity(130)
                .safetyStock(10)
                .version(1L)
                .historyId(UUID.randomUUID())
                .updatedAt(LocalDateTime.of(2026, 5, 26, 10, 0))
                .build();
    }

    private Map<String, Object> createBody(UUID warehouseId, UUID productOptionId) {
        return Map.of(
                "warehouseId", warehouseId.toString(),
                "productOptionId", productOptionId.toString(),
                "quantity", 100,
                "safetyStock", 10
        );
    }

    private Map<String, Object> adjustBody() {
        return Map.of("changeQuantity", 30, "changeType", "INBOUND", "reason", "정기 입고");
    }

    // ── POST /api/v1/inventory ────────────────────────────────────────────────

    @Test
    void 재고_생성_MASTER_201() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(inventoryService.createInventory(any(), any())).thenReturn(inventoryFixture(inventoryId, warehouseId));

        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(warehouseId, UUID.randomUUID()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.inventoryId").value(inventoryId.toString()))
                .andExpect(jsonPath("$.data.quantity").value(100));

        verify(inventoryService).createInventory(any(), any());
    }

    @Test
    void 재고_생성_HUB_MANAGER_201() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        when(inventoryService.createInventory(any(), any())).thenReturn(inventoryFixture(inventoryId, warehouseId));

        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Hub-Id", hubId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(warehouseId, UUID.randomUUID()))))
                .andExpect(status().isCreated());
    }

    @Test
    void 재고_생성_COMPANY_MANAGER_X_Company_Id_사용_201() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(inventoryService.createInventory(any(), any())).thenReturn(inventoryFixture(inventoryId, warehouseId));

        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", COMPANY_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(warehouseId, UUID.randomUUID()))))
                .andExpect(status().isCreated());

        verify(inventoryService).createInventory(any(), any());
    }

    @Test
    void 재고_생성_HUB_DELIVERY_MANAGER_403() throws Exception {
        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_DELIVERY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID(), UUID.randomUUID()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_생성_COMPANY_DELIVERY_MANAGER_403() throws Exception {
        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_DELIVERY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID(), UUID.randomUUID()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_생성_인증_없음_401() throws Exception {
        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID(), UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 재고_생성_warehouseId_누락_400() throws Exception {
        Map<String, Object> body = Map.of("productOptionId", UUID.randomUUID().toString(), "quantity", 10);
        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 재고_생성_중복_409() throws Exception {
        when(inventoryService.createInventory(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_INVENTORY));

        mockMvc.perform(post("/api/v1/inventory")
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID(), UUID.randomUUID()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 창고에 이미 동일한 상품 옵션의 재고가 존재합니다."));
    }

    // ── GET /api/v1/inventory/{id} ────────────────────────────────────────────

    @Test
    void 재고_단건_조회_200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventory(inventoryId)).thenReturn(inventoryFixture(inventoryId, UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/inventory/{id}", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inventoryId").value(inventoryId.toString()))
                .andExpect(jsonPath("$.data.quantity").value(100))
                .andExpect(jsonPath("$.data.availableQuantity").value(100));
    }

    @Test
    void 재고_단건_조회_없음_404() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventory(inventoryId))
                .thenThrow(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        mockMvc.perform(get("/api/v1/inventory/{id}", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("재고를 찾을 수 없습니다."));
    }

    @Test
    void 재고_단건_조회_인증_없음_401() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/inventory/warehouses/{warehouse_id} ──────────────────────

    @Test
    void 창고별_재고_목록_조회_200() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        when(inventoryService.getInventoriesByWarehouse(warehouseId))
                .thenReturn(List.of(
                        inventoryFixture(UUID.randomUUID(), warehouseId),
                        inventoryFixture(UUID.randomUUID(), warehouseId)
                ));

        mockMvc.perform(get("/api/v1/inventory/warehouses/{id}", warehouseId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_DELIVERY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void 창고별_재고_목록_없으면_빈_배열_200() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        when(inventoryService.getInventoriesByWarehouse(warehouseId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/inventory/warehouses/{id}", warehouseId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ── PATCH /api/v1/inventory/{id}/adjust ──────────────────────────────────

    @Test
    void 재고_조정_MASTER_200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.adjustInventory(any(), any(), isNull(), isNull()))
                .thenReturn(adjustFixture(inventoryId));

        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.previousQuantity").value(100))
                .andExpect(jsonPath("$.data.changeQuantity").value(30))
                .andExpect(jsonPath("$.data.currentQuantity").value(130));
    }

    @Test
    void 재고_조정_HUB_MANAGER_200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        when(inventoryService.adjustInventory(any(), any(), isNull(), any()))
                .thenReturn(adjustFixture(inventoryId));

        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Hub-Id", hubId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isOk());
    }

    @Test
    void 재고_조정_COMPANY_MANAGER_본인업체_200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.adjustInventory(any(), any(), any(), any()))
                .thenReturn(adjustFixture(inventoryId));

        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", COMPANY_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isOk());
    }

    @Test
    void 재고_조정_COMPANY_MANAGER_다른업체_403() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        doThrow(new BusinessException(ErrorCode.FORBIDDEN))
                .when(inventoryService).adjustInventory(any(), any(), any(), any());

        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_조정_HUB_DELIVERY_MANAGER_403() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_DELIVERY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_조정_COMPANY_DELIVERY_MANAGER_403() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_DELIVERY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_조정_재고없음_404() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND))
                .when(inventoryService).adjustInventory(any(), any(), any(), any());

        mockMvc.perform(patch("/api/v1/inventory/{id}/adjust", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustBody())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("재고를 찾을 수 없습니다."));
    }

    // ── DELETE /api/v1/inventory/{id} ─────────────────────────────────────────

    @Test
    void 재고_삭제_MASTER_200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        doNothing().when(inventoryService).deleteInventory(any(), any(), any());

        mockMvc.perform(delete("/api/v1/inventory/{id}", inventoryId)
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(inventoryService).deleteInventory(any(), any(), any());
    }

    @Test
    void 재고_삭제_HUB_MANAGER_200() throws Exception {
        doNothing().when(inventoryService).deleteInventory(any(), any(), any());

        mockMvc.perform(delete("/api/v1/inventory/{id}", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Hub-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void 재고_삭제_COMPANY_MANAGER_403() throws Exception {
        mockMvc.perform(delete("/api/v1/inventory/{id}", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_삭제_COMPANY_DELIVERY_MANAGER_403() throws Exception {
        mockMvc.perform(delete("/api/v1/inventory/{id}", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_DELIVERY_MANAGER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 재고_삭제_잔여재고_409() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVENTORY_HAS_STOCK))
                .when(inventoryService).deleteInventory(any(), any(), any());

        mockMvc.perform(delete("/api/v1/inventory/{id}", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("잔여 수량 또는 예약 수량이 있는 재고는 삭제할 수 없습니다."));
    }

    @Test
    void 재고_삭제_없음_404() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND))
                .when(inventoryService).deleteInventory(any(), any(), any());

        mockMvc.perform(delete("/api/v1/inventory/{id}", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/v1/inventory/company-orders/{id}/prepare ──────────────────

    @Test
    void 출고준비_MASTER_200() throws Exception {
        doNothing().when(shipmentService).prepare(any());

        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/prepare", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void 출고준비_HUB_MANAGER_200() throws Exception {
        doNothing().when(shipmentService).prepare(any());

        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/prepare", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    void 출고준비_COMPANY_MANAGER_403() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/prepare", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 출고준비_인증없음_401() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/prepare", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // ── PATCH /api/v1/inventory/company-orders/{id}/ship ─────────────────────

    @Test
    void 출고완료_MASTER_200() throws Exception {
        doNothing().when(shipmentService).ship(any());

        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/ship", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(shipmentService).ship(any());
    }

    @Test
    void 출고완료_HUB_MANAGER_200() throws Exception {
        doNothing().when(shipmentService).ship(any());

        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/ship", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    void 출고완료_COMPANY_MANAGER_403() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/ship", UUID.randomUUID())
                        .header("X-User-Id", USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 출고완료_인증없음_401() throws Exception {
        mockMvc.perform(patch("/api/v1/inventory/company-orders/{id}/ship", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
