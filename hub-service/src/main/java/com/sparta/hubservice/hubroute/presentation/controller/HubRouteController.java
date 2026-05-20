package com.sparta.hubservice.hubroute.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteCreateRequest;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteResponse;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private final HubRouteService hubRouteService;

    @PostMapping
    public ResponseEntity<ApiResponse<HubRouteResponse>> createHubRoute(
            @Valid @RequestBody HubRouteCreateRequest request) {
        HubRouteDto dto = hubRouteService.createHubRoute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(HubRouteResponse.from(dto)));
    }

    @GetMapping("/{route_id}")
    public ResponseEntity<ApiResponse<HubRouteResponse>> getHubRoute(
            @PathVariable UUID route_id) {
        HubRouteDto dto = hubRouteService.getHubRoute(route_id);
        return ResponseEntity.ok(ApiResponse.success(HubRouteResponse.from(dto)));
    }

    @PatchMapping("/{route_id}")
    public ResponseEntity<ApiResponse<HubRouteResponse>> updateHubRoute(
            @PathVariable UUID route_id,
            @RequestBody HubRouteUpdateRequest request) {
        HubRouteDto dto = hubRouteService.updateHubRoute(route_id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(HubRouteResponse.from(dto)));
    }

    @DeleteMapping("/{route_id}")
    public ResponseEntity<ApiResponse<Void>> deleteHubRoute(
            @PathVariable UUID route_id,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "system") String userName) {
        hubRouteService.deleteHubRoute(route_id, userName);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
