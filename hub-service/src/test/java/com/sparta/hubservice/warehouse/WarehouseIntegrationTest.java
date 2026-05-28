package com.sparta.hubservice.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hubservice.global.client.CompanyClient;
import com.sparta.hubservice.global.client.OrderClient;
import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import com.sparta.hubservice.warehouse.domain.core.WarehouseStatus;
import com.sparta.hubservice.warehouse.infrastructure.repository.WarehouseJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Warehouse 도메인 통합 테스트
 *
 * - H2 인메모리 DB (test/resources/application.yml)
 * - CompanyClient, OrderClient FeignClient를 @MockBean으로 대체 (컨텍스트 로딩 위해 필수)
 * - WarehouseService 는 hub 존재 여부를 별도로 검증하지 않으므로
 *   임의 hubId UUID를 직접 사용해 Warehouse 엔티티를 삽입
 * - @Transactional: 각 테스트 종료 후 자동 롤백
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class WarehouseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WarehouseJpaRepository warehouseRepository;

    @MockBean private CompanyClient companyClient;
    @MockBean private OrderClient orderClient;

    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UUID hubId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        hubId = UUID.randomUUID();
        Warehouse warehouse = Warehouse.builder()
                .hubId(hubId)
                .warehouseName("서울 중앙 창고")
                .address("서울특별시 중구 세종대로 110")
                .region("서울")
                .contactPhone("02-1234-5678")
                .status(WarehouseStatus.ACTIVE)
                .build();
        warehouseId = warehouseRepository.save(warehouse).getWarehouseId();
    }

    // ─── POST /api/v1/warehouses ──────────────────────────────────────────────

    @Test
    void 창고_생성_201_DB_반영() throws Exception {
        UUID anotherHubId = UUID.randomUUID();
        Map<String, Object> body = Map.of(
                "hubId", anotherHubId.toString(),
                "warehouseName", "부산 지역 창고",
                "address", "부산광역시 중구 중앙대로 2",
                "region", "부산",
                "contactPhone", "051-1234-5678",
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.warehouseId").isNotEmpty())
                .andExpect(jsonPath("$.data.hubId").value(anotherHubId.toString()))
                .andExpect(jsonPath("$.data.warehouseName").value("부산 지역 창고"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        assertThat(warehouseRepository.findAll()).hasSize(2);
    }

    @Test
    void 창고_생성_같은_허브_중복_409() throws Exception {
        Map<String, Object> body = Map.of(
                "hubId", hubId.toString(),
                "warehouseName", "중복 창고",
                "address", "서울특별시 중구",
                "region", "서울"
        );

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 허브에 이미 창고가 존재합니다."));
    }

    @Test
    void 창고_생성_hubId_누락_400() throws Exception {
        Map<String, Object> body = Map.of("warehouseName", "창고명만 있음");

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 창고_생성_MASTER_HUB_MANAGER_아님_403() throws Exception {
        Map<String, Object> body = Map.of(
                "hubId", UUID.randomUUID().toString(),
                "warehouseName", "권한 없는 창고"
        );

        mockMvc.perform(post("/api/v1/warehouses")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    // ─── GET /api/v1/warehouses ───────────────────────────────────────────────

    @Test
    void 전체_창고_목록_조회_페이지네이션() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses?page=0&size=10")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].warehouseName").value("서울 중앙 창고"));
    }

    // ─── GET /api/v1/warehouses/{warehouse_id} ────────────────────────────────

    @Test
    void 창고_단건_조회_DB_값_일치() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseId").value(warehouseId.toString()))
                .andExpect(jsonPath("$.data.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.data.warehouseName").value("서울 중앙 창고"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void 창고_단건_조회_없는_ID_404() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("물류 창고를 찾을 수 없습니다."));
    }

    // ─── GET /api/v1/warehouses/hub/{hub_id} ─────────────────────────────────

    @Test
    void 허브별_창고_조회_DB_값_일치() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses/hub/{hubId}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseId").value(warehouseId.toString()))
                .andExpect(jsonPath("$.data.hubId").value(hubId.toString()));
    }

    @Test
    void 허브별_창고_조회_없는_허브_404() throws Exception {
        mockMvc.perform(get("/api/v1/warehouses/hub/{hubId}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /api/v1/warehouses/{warehouse_id} ──────────────────────────────

    @Test
    void 창고_수정_후_DB_반영() throws Exception {
        Map<String, Object> body = Map.of(
                "warehouseName", "서울 중앙 창고 (수정)",
                "status", "INACTIVE"
        );

        mockMvc.perform(patch("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseName").value("서울 중앙 창고 (수정)"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        Warehouse updated = warehouseRepository.findById(warehouseId).orElseThrow();
        assertThat(updated.getWarehouseName()).isEqualTo("서울 중앙 창고 (수정)");
        assertThat(updated.getStatus()).isEqualTo(WarehouseStatus.INACTIVE);
    }

    @Test
    void 창고_수정_없는_ID_404() throws Exception {
        mockMvc.perform(patch("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("warehouseName", "없는 창고"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void 창고_수정_권한_없음_403() throws Exception {
        mockMvc.perform(patch("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("warehouseName", "수정 시도"))))
                .andExpect(status().isForbidden());
    }

    // ─── DELETE /api/v1/warehouses/{warehouse_id} ────────────────────────────

    @Test
    void 창고_삭제_소프트삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // soft delete 확인: deletedAt이 세팅되어 단건 조회 시 404
        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 창고_삭제_없는_ID_404() throws Exception {
        mockMvc.perform(delete("/api/v1/warehouses/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 창고_삭제_권한_없음_403() throws Exception {
        mockMvc.perform(delete("/api/v1/warehouses/{id}", warehouseId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }
}
