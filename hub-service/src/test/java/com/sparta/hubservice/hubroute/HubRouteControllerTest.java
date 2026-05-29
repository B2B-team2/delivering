package com.sparta.hubservice.hubroute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.config.SecurityConfig;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.controller.HubRouteController;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HubRouteController.class)
@Import(SecurityConfig.class)
class HubRouteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private HubRouteService hubRouteService;

    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final UUID ADMIN_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // SecurityConfig가 anyRequest().authenticated()이므로 모든 요청에 인증 헤더 필요
    // CUD: X-User-Role=MASTER 필수 / R: 아무 역할이나 가능
    private static final String ROLE_MASTER         = "MASTER";
    private static final String ROLE_HUB_MANAGER    = "HUB_MANAGER";
    private static final String ROLE_COMPANY_MANAGER = "COMPANY_MANAGER";

    private HubRouteDto routeDto(UUID routeId) {
        return HubRouteDto.builder()
                .routeId(routeId)
                .fromHub(HubRouteDto.HubInfoDto.builder()
                        .hubId(UUID.randomUUID())
                        .name("서울 중앙 허브")
                        .address("서울특별시 중구 세종대로 110")
                        .build())
                .toHub(HubRouteDto.HubInfoDto.builder()
                        .hubId(UUID.randomUUID())
                        .name("경기 북부 센터")
                        .address("경기도 의정부시 평화로 100")
                        .build())
                .duration("02:00:00")
                .distance(new BigDecimal("350.50"))
                .createdAt(LocalDateTime.of(2026, 5, 14, 9, 0))
                .createdBy(SYSTEM_UUID)
                .updatedAt(LocalDateTime.of(2026, 5, 14, 9, 0))
                .updatedBy(SYSTEM_UUID)
                .build();
    }

    private Map<String, Object> routeBody() {
        return Map.of(
                "fromHubId", UUID.randomUUID().toString(),
                "toHubId", UUID.randomUUID().toString(),
                "duration", 120,
                "distance", 350.50
        );
    }

    // ── POST /api/v1/hub-routes ───────────────────────────────────────────────

    @Test
    void 허브_경로_생성_201() throws Exception {
        UUID routeId = UUID.randomUUID();
        when(hubRouteService.createHubRoute(any())).thenReturn(routeDto(routeId));

        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.routeId").value(routeId.toString()))
                .andExpect(jsonPath("$.data.fromHub.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.toHub.name").value("경기 북부 센터"))
                .andExpect(jsonPath("$.data.duration").value("02:00:00"))
                .andExpect(jsonPath("$.data.createdBy").value(SYSTEM_UUID.toString()));
    }

    @Test
    void 허브_경로_생성_MASTER_아님_403() throws Exception {
        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_HUB_MANAGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeBody())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 허브_경로_생성_인증_없음_401() throws Exception {
        mockMvc.perform(post("/api/v1/hub-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeBody())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 허브_경로_생성_중복_409() throws Exception {
        when(hubRouteService.createHubRoute(any()))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_ROUTE));

        mockMvc.perform(post("/api/v1/hub-routes")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeBody())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 구간의 허브 경로가 이미 존재합니다."));
    }

    // ── GET /api/v1/hub-routes ────────────────────────────────────────────────

    @Test
    void 전체_경로_조회_200_페이지네이션() throws Exception {
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();
        Page<HubRouteDto> page = new PageImpl<>(List.of(routeDto(r1), routeDto(r2)));
        when(hubRouteService.getAllHubRoutes(any(Pageable.class))).thenReturn(page);

        // 조회는 MASTER가 아니어도 가능
        mockMvc.perform(get("/api/v1/hub-routes?page=0&size=10")
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].fromHub.name").value("서울 중앙 허브"))
                .andExpect(jsonPath("$.data.content[0].duration").value("02:00:00"));
    }

    // ── GET /api/v1/hub-routes/{route_id} ─────────────────────────────────────

    @Test
    void 허브_경로_단건_조회_200() throws Exception {
        UUID routeId = UUID.randomUUID();
        when(hubRouteService.getHubRoute(routeId)).thenReturn(routeDto(routeId));

        mockMvc.perform(get("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routeId").value(routeId.toString()))
                .andExpect(jsonPath("$.data.fromHub").isMap())
                .andExpect(jsonPath("$.data.toHub").isMap());
    }

    @Test
    void 허브_경로_단건_조회_없음_404() throws Exception {
        UUID routeId = UUID.randomUUID();
        when(hubRouteService.getHubRoute(routeId))
                .thenThrow(new BusinessException(ErrorCode.ROUTE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("허브 경로를 찾을 수 없습니다."));
    }

    // ── PATCH /api/v1/hub-routes/{route_id} ───────────────────────────────────

    @Test
    void 허브_경로_수정_200() throws Exception {
        UUID routeId = UUID.randomUUID();
        HubRouteDto updated = HubRouteDto.builder()
                .routeId(routeId)
                .fromHub(HubRouteDto.HubInfoDto.builder()
                        .hubId(UUID.randomUUID()).name("서울 중앙 허브").address("서울 주소").build())
                .toHub(HubRouteDto.HubInfoDto.builder()
                        .hubId(UUID.randomUUID()).name("경기 북부 센터").address("경기 주소").build())
                .duration("02:30:00")
                .distance(new BigDecimal("350.50"))
                .createdAt(LocalDateTime.of(2026, 5, 14, 9, 0))
                .createdBy(SYSTEM_UUID)
                .updatedAt(LocalDateTime.of(2026, 5, 22, 10, 0))
                .updatedBy(ADMIN_UUID)
                .build();
        when(hubRouteService.updateHubRoute(any(), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/hub-routes/{id}", routeId)
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("duration", 150))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duration").value("02:30:00"))
                .andExpect(jsonPath("$.data.updatedBy").value(ADMIN_UUID.toString()));
    }

    @Test
    void 허브_경로_수정_MASTER_아님_403() throws Exception {
        mockMvc.perform(patch("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_COMPANY_MANAGER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("duration", 150))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 허브_경로_수정_없음_404() throws Exception {
        when(hubRouteService.updateHubRoute(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.ROUTE_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("duration", 150))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/hub-routes/{route_id} ──────────────────────────────────

    @Test
    void 허브_경로_삭제_200() throws Exception {
        doNothing().when(hubRouteService).deleteHubRoute(any(), any());

        mockMvc.perform(delete("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void 허브_경로_삭제_MASTER_아님_403() throws Exception {
        mockMvc.perform(delete("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_HUB_MANAGER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 허브_경로_삭제_없음_404() throws Exception {
        doThrow(new BusinessException(ErrorCode.ROUTE_NOT_FOUND))
                .when(hubRouteService).deleteHubRoute(any(), any());

        mockMvc.perform(delete("/api/v1/hub-routes/{id}", UUID.randomUUID())
                        .header("X-User-Id", ADMIN_UUID.toString())
                        .header("X-User-Role", ROLE_MASTER))
                .andExpect(status().isNotFound());
    }
}
