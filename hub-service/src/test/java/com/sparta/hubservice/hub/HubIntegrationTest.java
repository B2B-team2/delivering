package com.sparta.hubservice.hub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hubservice.global.client.CompanyClient;
import com.sparta.hubservice.global.client.OrderClient;
import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.infrastructure.repository.HubJpaRepository;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Hub 도메인 통합 테스트
 *
 * - H2 인메모리 DB (test/resources/application.yml)
 * - CompanyClient(company-service), OrderClient(order-service) FeignClient를 @MockBean으로 대체
 *   → company-service 없이도 hub 삭제 시 업체 존재 여부 검증 흐름 테스트 가능
 * - @Transactional: 각 테스트 종료 후 자동 롤백 → 테스트 간 DB 격리
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class HubIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private HubJpaRepository hubRepository;

    @MockBean private CompanyClient companyClient;
    @MockBean private OrderClient orderClient;

    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UUID hubId;

    @BeforeEach
    void setUp() {
        Hub hub = Hub.builder()
                .name("서울 중앙 허브")
                .hubType(HubType.CENTRAL)
                .address("서울특별시 중구 세종대로 110")
                .latitude(37.5665)
                .longitude(126.9780)
                .contactPhone("02-1234-5678")
                .status(HubStatus.ACTIVE)
                .build();
        hubId = hubRepository.save(hub).getHubId();
    }

    // ─── POST /api/v1/hubs ────────────────────────────────────────────────────

    @Test
    void 허브_생성_201_DB_반영() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "부산 지역 허브",
                "hubType", "REGIONAL",
                "address", "부산광역시 중구 중앙대로 2",
                "location", Map.of("latitude", 35.1796, "longitude", 129.0756),
                "contactPhone", "051-1234-5678"
        );

        mockMvc.perform(post("/api/v1/hubs")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.hubId").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("부산 지역 허브"))
                .andExpect(jsonPath("$.data.hubType").value("REGIONAL"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.location.latitude").value(35.1796))
                .andExpect(jsonPath("$.data.location.longitude").value(129.0756));

        assertThat(hubRepository.findAllByDeletedAtIsNull()).hasSize(2);
    }

    @Test
    void 허브_생성_필수_필드_누락_400() throws Exception {
        Map<String, Object> body = Map.of(
                "hubType", "REGIONAL",
                "address", "부산광역시 중구 중앙대로 2",
                "location", Map.of("latitude", 35.1796, "longitude", 129.0756)
        );

        mockMvc.perform(post("/api/v1/hubs")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 허브_생성_MASTER_아님_403() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "부산 허브",
                "hubType", "REGIONAL",
                "address", "부산광역시 중구 중앙대로 2",
                "location", Map.of("latitude", 35.1796, "longitude", 129.0756)
        );

        mockMvc.perform(post("/api/v1/hubs")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 허브_생성_인증_없음_401() throws Exception {
        mockMvc.perform(post("/api/v1/hubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/v1/hubs ────────────────────────────────────────────────────

    @Test
    void 전체_허브_목록_조회_페이지네이션() throws Exception {
        mockMvc.perform(get("/api/v1/hubs?page=0&size=10")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.content[0].hubType").value("CENTRAL"));
    }

    // ─── GET /api/v1/hubs/{hub_id} ───────────────────────────────────────────

    @Test
    void 허브_단건_조회_DB_값_일치() throws Exception {
        mockMvc.perform(get("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.data.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.hubType").value("CENTRAL"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.location.latitude").value(37.5665))
                .andExpect(jsonPath("$.data.location.longitude").value(126.978));
    }

    @Test
    void 허브_단건_조회_없는_ID_404() throws Exception {
        mockMvc.perform(get("/api/v1/hubs/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("허브를 찾을 수 없습니다."));
    }

    // ─── PATCH /api/v1/hubs/{hub_id} ─────────────────────────────────────────

    @Test
    void 허브_수정_후_DB_반영() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "서울 중앙 허브 (수정)",
                "status", "MAINTENANCE"
        );

        mockMvc.perform(patch("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("서울 중앙 허브 (수정)"))
                .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));

        Hub updated = hubRepository.findByHubIdAndDeletedAtIsNull(hubId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("서울 중앙 허브 (수정)");
        assertThat(updated.getStatus()).isEqualTo(HubStatus.MAINTENANCE);
    }

    @Test
    void 허브_수정_MASTER_아님_403() throws Exception {
        mockMvc.perform(patch("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "수정 시도"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void 허브_수정_없는_ID_404() throws Exception {
        mockMvc.perform(patch("/api/v1/hubs/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "없는 허브"))))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/v1/hubs/{hub_id} ────────────────────────────────────────

    @Test
    void 허브_삭제_소속_업체_없으면_소프트삭제_성공() throws Exception {
        // CompanyClient(company-service) mock: 소속 업체 없음
        given(companyClient.existsCompaniesByHubId(hubId)).willReturn(false);

        mockMvc.perform(delete("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // soft delete → deletedAt 세팅됨, findByHubIdAndDeletedAtIsNull 으로 조회 불가
        assertThat(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).isEmpty();
    }

    @Test
    void 허브_삭제_소속_업체_있으면_409_DB_불변() throws Exception {
        // CompanyClient(company-service) mock: 소속 업체 있음
        given(companyClient.existsCompaniesByHubId(hubId)).willReturn(true);

        mockMvc.perform(delete("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isConflict());

        assertThat(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).isPresent();
    }

    @Test
    void 허브_삭제_없는_ID_404() throws Exception {
        mockMvc.perform(delete("/api/v1/hubs/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 허브_삭제_MASTER_아님_403() throws Exception {
        mockMvc.perform(delete("/api/v1/hubs/{id}", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    // ─── GET /api/v1/hubs/{hub_id}/routes ────────────────────────────────────

    @Test
    void 허브별_출발_경로_목록_조회_200() throws Exception {
        mockMvc.perform(get("/api/v1/hubs/{id}/routes", hubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
