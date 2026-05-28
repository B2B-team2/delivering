package com.sparta.hubservice.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.config.SecurityConfig;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.application.service.WarehouseService;
import com.sparta.hubservice.warehouse.presentation.controller.WarehouseController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
@Import(SecurityConfig.class)
class WarehouseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private WarehouseService warehouseService;

    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final UUID ADMIN_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // CUD: MASTER 또는 HUB_MANAGER만 허용 / R: 모든 인증된 역할 허용
    private static final String ROLE_MASTER          = "MASTER";
    private static final String ROLE_HUB_MANAGER     = "HUB_MANAGER";
    private static final String ROLE_COMPANY_MANAGER = "COMPANY_MANAGER";

    private WarehouseDto fixture(UUID warehouseId, UUID hubId) {
        return WarehouseDto.builder()
                .warehouseId(warehouseId)
                .hubId(hubId)
                .warehouseName("서울 물류창고")
                .address("서울시 강남구")
                .region("SEOUL")
                .contactPhone("02-1234-5678")
                .status("ACTIVE")
                .createdAt(LocalDateTime.of(2026, 5, 22, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 5, 22, 10, 0))
                .updatedBy(SYSTEM_UUID)
                .build();
    }

    private Map<String, Object> createBody(UUID hubId) {
        return Map.of(
                "hubId", hubId.toString(),
                "warehouseName", "서울 물류창고",
                "address", "서울시 강남구",
                "region", "SEOUL",
                "contactPhone", "02-1234-5678",
                "status", "ACTIVE"
        );
    }

    // ── POST /api/v1/warehouses ───────────────────────────────────────────────

    @Test
    void 창고_생성_MASTER_201() throws Exception {
        UUID hubId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(warehouseService.createWarehouse(any())).thenReturn(fixture(warehouseId, hubId));

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(hubId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.warehouseId").value(warehouseId.toString()))
                .andExpect(jsonPath("$.data.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(warehouseService).createWarehouse(any());
    }

    @Test
    void 창고_생성_HUB_MANAGER_201() throws Exception {
        UUID hubId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(warehouseService.createWarehouse(any())).thenReturn(fixture(warehouseId, hubId));

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_HUB_MANAGER)
                        .header("X-Hub-Id", hubId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(hubId))))
                .andExpect(status().isCreated());
    }

    @Test
    void 창고_생성_권한_없음_403() throws Exception {
        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 창고_생성_hubId_누락_400() throws Exception {
        Map<String, String> body = Map.of("warehouseName", "서울 물류창고");
        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 창고_생성_허브당_중복_409() throws Exception {
        when(warehouseService.createWarehouse(any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_WAREHOUSE));

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody(UUID.randomUUID()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 허브에 이미 창고가 존재합니다."));
    }

    // ── GET /api/v1/warehouses ────────────────────────────────────────────────

    @Test
    void 창고_목록_조회_200_페이지네이션() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Page<WarehouseDto> page = new PageImpl<>(List.of(
                fixture(id1, UUID.randomUUID()),
                fixture(id2, UUID.randomUUID())
        ));
        when(warehouseService.getAllWarehouses(any(Pageable.class))).thenReturn(page);

        // 조회는 모든 역할 허용
        mockMvc.perform(get("/api/v1/warehouses?page=0&size=10")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    // ── GET /api/v1/warehouses/{warehouse_id} ─────────────────────────────────

    @Test
    void 창고_단건_조회_200() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        when(warehouseService.getWarehouse(warehouseId)).thenReturn(fixture(warehouseId, UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseId").value(warehouseId.toString()))
                .andExpect(jsonPath("$.data.warehouseName").value("서울 물류창고"));
    }

    @Test
    void 창고_단건_조회_없음_404() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        when(warehouseService.getWarehouse(warehouseId))
                .thenThrow(new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("물류 창고를 찾을 수 없습니다."));
    }

    // ── GET /api/v1/warehouses/hub/{hub_id} ───────────────────────────────────

    @Test
    void 허브별_창고_조회_200() throws Exception {
        UUID hubId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(warehouseService.getWarehouseByHubId(hubId)).thenReturn(fixture(warehouseId, hubId));

        mockMvc.perform(get("/api/v1/warehouses/hub/{hubId}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(hubId.toString()));
    }

    @Test
    void 허브별_창고_조회_없음_404() throws Exception {
        UUID hubId = UUID.randomUUID();
        when(warehouseService.getWarehouseByHubId(hubId))
                .thenThrow(new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/warehouses/hub/{hubId}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/v1/warehouses/{warehouse_id} ───────────────────────────────

    @Test
    void 창고_수정_MASTER_200() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        WarehouseDto updated = WarehouseDto.builder()
                .warehouseId(warehouseId)
                .hubId(UUID.randomUUID())
                .warehouseName("수정된 창고명")
                .address("서울시 서초구")
                .region("SEOUL")
                .contactPhone("02-9999-8888")
                .status("INACTIVE")
                .createdAt(LocalDateTime.of(2026, 5, 22, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 5, 22, 12, 0))
                .updatedBy(ADMIN_UUID)
                .build();
        when(warehouseService.updateWarehouse(any(), any(), any())).thenReturn(updated);

        Map<String, String> body = Map.of("warehouseName", "수정된 창고명", "status", "INACTIVE");
        mockMvc.perform(patch("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseName").value("수정된 창고명"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.updatedBy").value(ADMIN_UUID.toString()));
    }

    @Test
    void 창고_수정_권한_없음_403() throws Exception {
        mockMvc.perform(patch("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 창고_수정_없음_404() throws Exception {
        when(warehouseService.updateWarehouse(any(), any(), any()))
                .thenThrow(new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/warehouses/{warehouse_id} ──────────────────────────────

    @Test
    void 창고_삭제_MASTER_200() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        doNothing().when(warehouseService).deleteWarehouse(any(), any(), any());

        mockMvc.perform(delete("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(warehouseService).deleteWarehouse(any(), any(), any());
    }

    @Test
    void 창고_삭제_HUB_MANAGER_200() throws Exception {
        doNothing().when(warehouseService).deleteWarehouse(any(), any(), any());

        mockMvc.perform(delete("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_HUB_MANAGER)
                        .header("X-Hub-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void 창고_삭제_권한_없음_403() throws Exception {
        mockMvc.perform(delete("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 창고_삭제_없음_404() throws Exception {
        doThrow(new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND))
                .when(warehouseService).deleteWarehouse(any(), any(), any());

        mockMvc.perform(delete("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER))
                .andExpect(status().isNotFound());
    }
}
