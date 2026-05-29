package com.sparta.hubservice.hubroute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.hubservice.global.client.CompanyClient;
import com.sparta.hubservice.global.client.OrderClient;
import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.infrastructure.repository.HubJpaRepository;
import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import com.sparta.hubservice.hubroute.infrastructure.repository.HubRouteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HubRoute 도메인 통합 테스트
 *
 * - H2 인메모리 DB (test/resources/application.yml)
 * - CompanyClient, OrderClient FeignClient를 @MockBean으로 대체
 * - HubRouteService 는 경로 생성/조회 시 HubReader(→ HubJpaRepository)를 통해
 *   허브 이름·주소를 읽으므로 Hub 엔티티를 사전에 삽입
 * - @Transactional: 각 테스트 종료 후 자동 롤백
 *
 * 경로 탐색 로직 (findRoute):
 *  - distance < 200km → 직행 반환
 *  - distance >= 200km → CENTRAL 허브 경유 최단 경로 탐색
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class HubRouteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private HubJpaRepository hubRepository;
    @Autowired private HubRouteJpaRepository hubRouteRepository;

    @MockBean private CompanyClient companyClient;
    @MockBean private OrderClient orderClient;

    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UUID fromHubId;
    private UUID toHubId;
    private UUID centralHubId;
    private UUID routeId;

    @BeforeEach
    void setUp() {
        Hub fromHub = Hub.builder()
                .name("서울 중앙 허브")
                .hubType(HubType.CENTRAL)
                .address("서울특별시 중구 세종대로 110")
                .latitude(37.5665)
                .longitude(126.9780)
                .contactPhone("02-1234-5678")
                .status(HubStatus.ACTIVE)
                .build();
        fromHubId = hubRepository.save(fromHub).getHubId();

        Hub toHub = Hub.builder()
                .name("경기 북부 센터")
                .hubType(HubType.REGIONAL)
                .address("경기도 의정부시 평화로 100")
                .latitude(37.7381)
                .longitude(127.0337)
                .contactPhone("031-111-2222")
                .status(HubStatus.ACTIVE)
                .build();
        toHubId = hubRepository.save(toHub).getHubId();

        Hub centralHub = Hub.builder()
                .name("대전 중앙 허브")
                .hubType(HubType.CENTRAL)
                .address("대전광역시 서구 둔산대로 100")
                .latitude(36.3504)
                .longitude(127.3845)
                .contactPhone("042-111-2222")
                .status(HubStatus.ACTIVE)
                .build();
        centralHubId = hubRepository.save(centralHub).getHubId();

        // 직행 경로 (150km < 200km 임계값 → 직행 탐색)
        HubRoute route = HubRoute.builder()
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .duration(90)
                .distance(new BigDecimal("150.5"))
                .build();
        routeId = hubRouteRepository.save(route).getRouteId();
    }

    // ─── POST /api/v1/hub-routes ──────────────────────────────────────────────

    @Test
    void 허브_경로_생성_201_DB_반영() throws Exception {
        Map<String, Object> body = Map.of(
                "fromHubId", fromHubId.toString(),
                "toHubId", centralHubId.toString(),
                "duration", 120,
                "distance", 200.0
        );

        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.routeId").isNotEmpty())
                .andExpect(jsonPath("$.data.fromHub.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.toHub.name").value("대전 중앙 허브"))
                .andExpect(jsonPath("$.data.duration").value("02:00:00"))
                .andExpect(jsonPath("$.data.distance").value(200.0));

        assertThat(hubRouteRepository.findAll()).hasSize(2);
    }

    @Test
    void 허브_경로_생성_중복_409() throws Exception {
        Map<String, Object> body = Map.of(
                "fromHubId", fromHubId.toString(),
                "toHubId", toHubId.toString(),
                "duration", 90,
                "distance", 150.5
        );

        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 구간의 허브 경로가 이미 존재합니다."));
    }

    @Test
    void 허브_경로_생성_MASTER_아님_403() throws Exception {
        Map<String, Object> body = Map.of(
                "fromHubId", fromHubId.toString(),
                "toHubId", centralHubId.toString(),
                "duration", 120,
                "distance", 200.0
        );

        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ─── GET /api/v1/hub-routes ───────────────────────────────────────────────

    @Test
    void 전체_경로_목록_조회_페이지네이션() throws Exception {
        mockMvc.perform(get("/api/v1/hub-routes?page=0&size=10")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].fromHub.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.content[0].toHub.name").value("경기 북부 센터"))
                .andExpect(jsonPath("$.data.content[0].duration").value("01:30:00"));
    }

    // ─── GET /api/v1/hub-routes/{route_id} ───────────────────────────────────

    @Test
    void 경로_단건_조회_DB_값_일치() throws Exception {
        mockMvc.perform(get("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routeId").value(routeId.toString()))
                .andExpect(jsonPath("$.data.fromHub.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.toHub.name").value("경기 북부 센터"))
                .andExpect(jsonPath("$.data.duration").value("01:30:00"))
                .andExpect(jsonPath("$.data.distance").value(150.5));
    }

    @Test
    void 경로_단건_조회_없는_ID_404() throws Exception {
        mockMvc.perform(get("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("허브 경로를 찾을 수 없습니다."));
    }

    // ─── PATCH /api/v1/hub-routes/{route_id} ─────────────────────────────────

    @Test
    void 경로_수정_후_DB_반영() throws Exception {
        Map<String, Object> body = Map.of("duration", 150, "distance", 170.0);

        mockMvc.perform(patch("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duration").value("02:30:00"))
                .andExpect(jsonPath("$.data.distance").value(170.0));

        HubRoute updated = hubRouteRepository.findById(routeId).orElseThrow();
        assertThat(updated.getDuration()).isEqualTo(150);
        assertThat(updated.getDistance()).isEqualByComparingTo(new BigDecimal("170.0"));
    }

    // ─── DELETE /api/v1/hub-routes/{route_id} ────────────────────────────────

    @Test
    void 경로_삭제_소프트삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // soft delete → 단건 조회 시 404
        mockMvc.perform(get("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 경로_삭제_없는_ID_404() throws Exception {
        mockMvc.perform(delete("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/v1/hub-routes/search (Dijkstra 직행) ─────────────────────

    @Test
    void 경로_탐색_직행_150km_단일_구간_반환() throws Exception {
        Map<String, Object> body = Map.of(
                "fromHubId", fromHubId.toString(),
                "toHubId", toHubId.toString()
        );

        mockMvc.perform(post("/api/v1/hub-routes/search")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromHubId").value(fromHubId.toString()))
                .andExpect(jsonPath("$.data.toHubId").value(toHubId.toString()))
                .andExpect(jsonPath("$.data.totalDuration").value("01:30:00"))
                .andExpect(jsonPath("$.data.totalDistance").value(150.5))
                .andExpect(jsonPath("$.data.routes").isArray())
                .andExpect(jsonPath("$.data.routes.length()").value(1))
                .andExpect(jsonPath("$.data.routes[0].sequence").value(1))
                .andExpect(jsonPath("$.data.routes[0].fromHubName").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.routes[0].toHubName").value("경기 북부 센터"));
    }

    @Test
    void 경로_탐색_200km_이상_CENTRAL_경유_2구간_반환() throws Exception {
        // 경기북부(REGIONAL) → 대전(CENTRAL): 100km, 90min
        HubRoute leg1Route = HubRoute.builder()
                .fromHubId(toHubId)
                .toHubId(centralHubId)
                .duration(90)
                .distance(new BigDecimal("100.0"))
                .build();
        hubRouteRepository.save(leg1Route);

        // 대전(CENTRAL) → 서울(CENTRAL): 150km, 120min
        HubRoute leg2Route = HubRoute.builder()
                .fromHubId(centralHubId)
                .toHubId(fromHubId)
                .duration(120)
                .distance(new BigDecimal("150.0"))
                .build();
        hubRouteRepository.save(leg2Route);

        // 경기북부 → 서울 직행: 300km (≥200km → CENTRAL 경유 탐색)
        HubRoute longDirect = HubRoute.builder()
                .fromHubId(toHubId)
                .toHubId(fromHubId)
                .duration(200)
                .distance(new BigDecimal("300.0"))
                .build();
        hubRouteRepository.save(longDirect);

        // buildPath(경기북부→서울): direct=300km≥200 → central후보=[대전]
        //   leg1=경기북부→대전(100km) ✓, leg2=대전→서울(150km) ✓ → 2구간 반환
        Map<String, Object> body = Map.of(
                "fromHubId", toHubId.toString(),
                "toHubId", fromHubId.toString()
        );

        mockMvc.perform(post("/api/v1/hub-routes/search")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routes").isArray())
                .andExpect(jsonPath("$.data.routes.length()").value(2))
                .andExpect(jsonPath("$.data.routes[0].fromHubName").value("경기 북부 센터"))
                .andExpect(jsonPath("$.data.routes[0].toHubName").value("대전 중앙 허브"))
                .andExpect(jsonPath("$.data.routes[1].fromHubName").value("대전 중앙 허브"))
                .andExpect(jsonPath("$.data.routes[1].toHubName").value("서울 중앙 허브"));
    }

    @Test
    void 경로_탐색_없는_경로_404() throws Exception {
        Map<String, Object> body = Map.of(
                "fromHubId", fromHubId.toString(),
                "toHubId", UUID.randomUUID().toString()
        );

        mockMvc.perform(post("/api/v1/hub-routes/search")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("허브 경로를 찾을 수 없습니다."));
    }

    @Test
    void 경로_탐색_필수_필드_누락_400() throws Exception {
        mockMvc.perform(post("/api/v1/hub-routes/search")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fromHubId", fromHubId.toString()))))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/v1/hubs/{hub_id}/routes ────────────────────────────────────

    @Test
    void 허브별_출발_경로_목록_조회() throws Exception {
        mockMvc.perform(get("/api/v1/hubs/{id}/routes", fromHubId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].routeId").value(routeId.toString()));
    }
}
