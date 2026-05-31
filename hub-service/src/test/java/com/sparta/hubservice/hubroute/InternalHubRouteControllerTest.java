package com.sparta.hubservice.hubroute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.config.SecurityConfig;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hubroute.application.dto.RouteSearchResult;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.controller.InternalHubRouteController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalHubRouteController.class)
@Import(SecurityConfig.class)
class InternalHubRouteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private HubRouteService hubRouteService;

    private RouteSearchResult singleSegmentResult(UUID fromHubId, UUID toHubId) {
        return RouteSearchResult.builder()
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .totalDuration(90)
                .totalDistance(new BigDecimal("150.5"))
                .routes(List.of(
                        RouteSearchResult.Segment.builder()
                                .sequence(1)
                                .routeId(UUID.randomUUID())
                                .fromHubId(fromHubId)
                                .fromHubName("서울 허브")
                                .toHubId(toHubId)
                                .toHubName("부산 허브")
                                .duration(90)
                                .distance(new BigDecimal("150.5"))
                                .build()
                ))
                .build();
    }

    // ── 정상 케이스 ──────────────────────────────────────────────────────────

    @Test
    void search_직행경로_200() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        when(hubRouteService.findRoute(from, to)).thenReturn(singleSegmentResult(from, to));

        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fromHubId", from.toString(), "toHubId", to.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromHubId").value(from.toString()))
                .andExpect(jsonPath("$.toHubId").value(to.toString()))
                .andExpect(jsonPath("$.totalDuration").value("01:30:00"))
                .andExpect(jsonPath("$.totalDistance").value(150.5))
                .andExpect(jsonPath("$.routes").isArray())
                .andExpect(jsonPath("$.routes[0].sequence").value(1))
                .andExpect(jsonPath("$.routes[0].fromHubName").value("서울 허브"))
                .andExpect(jsonPath("$.routes[0].toHubName").value("부산 허브"));
    }

    @Test
    void search_경유경로_routes_2개() throws Exception {
        UUID from = UUID.randomUUID();
        UUID via = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        RouteSearchResult result = RouteSearchResult.builder()
                .fromHubId(from)
                .toHubId(to)
                .totalDuration(180)
                .totalDistance(new BigDecimal("350.0"))
                .routes(List.of(
                        RouteSearchResult.Segment.builder()
                                .sequence(1).routeId(UUID.randomUUID())
                                .fromHubId(from).fromHubName("서울 허브")
                                .toHubId(via).toHubName("대전 허브")
                                .duration(90).distance(new BigDecimal("170.0")).build(),
                        RouteSearchResult.Segment.builder()
                                .sequence(2).routeId(UUID.randomUUID())
                                .fromHubId(via).fromHubName("대전 허브")
                                .toHubId(to).toHubName("부산 허브")
                                .duration(90).distance(new BigDecimal("180.0")).build()
                ))
                .build();
        when(hubRouteService.findRoute(from, to)).thenReturn(result);

        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fromHubId", from.toString(), "toHubId", to.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDuration").value("03:00:00"))
                .andExpect(jsonPath("$.routes.length()").value(2))
                .andExpect(jsonPath("$.routes[1].sequence").value(2));
    }

    // ── 유효성 검사 ───────────────────────────────────────────────────────────

    @Test
    void search_fromHubId_누락_400() throws Exception {
        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("toHubId", UUID.randomUUID().toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_toHubId_누락_400() throws Exception {
        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fromHubId", UUID.randomUUID().toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_빈_body_400() throws Exception {
        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── 에러 케이스 ───────────────────────────────────────────────────────────

    @Test
    void search_경로_없음_404() throws Exception {
        when(hubRouteService.findRoute(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.ROUTE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/internal/hub-routes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fromHubId", UUID.randomUUID().toString(),
                                       "toHubId", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("허브 경로를 찾을 수 없습니다."));
    }
}
