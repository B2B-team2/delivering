package com.sparta.hubservice.hubroute.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchRequest;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/hub-routes")
@RequiredArgsConstructor
public class InternalHubRouteController {

    private final HubRouteService hubRouteService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<RouteSearchResponse>> searchRoute(
            @Valid @RequestBody RouteSearchRequest request) {
        RouteSearchResponse response = RouteSearchResponse.from(
                hubRouteService.findRoute(request.getFromHubId(), request.getToHubId()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
