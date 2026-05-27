package com.sparta.hubservice.hubroute.presentation.controller;

import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchRequest;
import com.sparta.hubservice.hubroute.presentation.dto.RouteSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public RouteSearchResponse searchRoute(
            @Valid @RequestBody RouteSearchRequest request) {
        return RouteSearchResponse.from(
                hubRouteService.findRoute(request.getFromHubId(), request.getToHubId()));
    }
}
