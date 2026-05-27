package com.sparta.hubservice.hubroute.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteCreateRequest;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteResponse;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteUpdateRequest;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchRequest;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    private final HubRouteService hubRouteService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<RouteSearchResponse>> searchRoute(
            @Valid @RequestBody RouteSearchRequest request) {
        RouteSearchResponse response = RouteSearchResponse.from(
                hubRouteService.findRoute(request.getFromHubId(), request.getToHubId()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HubRouteResponse>> createHubRoute(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody HubRouteCreateRequest request) {
        requireMaster(role);
        HubRouteDto dto = hubRouteService.createHubRoute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(HubRouteResponse.from(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HubRouteResponse>>> getAllHubRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        int validSize = ALLOWED_SIZES.contains(size) ? size : 10;
        String[] sortParts = sort.split(",");
        Sort sortObj = sortParts.length > 1
                ? Sort.by(Sort.Direction.fromString(sortParts[1].trim()), sortParts[0].trim())
                : Sort.by(sortParts[0].trim());
        PageResponse<HubRouteResponse> result = new PageResponse<>(
                hubRouteService.getAllHubRoutes(PageRequest.of(page, validSize, sortObj))
                        .map(HubRouteResponse::from));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{route_id}")
    public ResponseEntity<ApiResponse<HubRouteResponse>> getHubRoute(
            @PathVariable UUID route_id) {
        HubRouteDto dto = hubRouteService.getHubRoute(route_id);
        return ResponseEntity.ok(ApiResponse.success(HubRouteResponse.from(dto)));
    }

    @PatchMapping("/{route_id}")
    public ResponseEntity<ApiResponse<HubRouteResponse>> updateHubRoute(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID route_id,
            @RequestBody HubRouteUpdateRequest request) {
        requireMaster(role);
        HubRouteDto dto = hubRouteService.updateHubRoute(route_id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(HubRouteResponse.from(dto)));
    }

    @DeleteMapping("/{route_id}")
    public ResponseEntity<ApiResponse<Void>> deleteHubRoute(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID route_id,
            @RequestHeader("X-User-Id") UUID userId) {
        requireMaster(role);
        hubRouteService.deleteHubRoute(route_id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private void requireMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
