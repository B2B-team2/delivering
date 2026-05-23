package com.sparta.hubservice.hub.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.hubservice.hub.application.dto.HubDto;
import com.sparta.hubservice.hub.application.service.HubService;
import com.sparta.hubservice.hub.presentation.dto.HubCreateRequest;
import com.sparta.hubservice.hub.presentation.dto.HubResponse;
import com.sparta.hubservice.hub.presentation.dto.HubUpdateRequest;
import com.sparta.hubservice.hubroute.application.service.HubRouteService;
import com.sparta.hubservice.hubroute.presentation.dto.HubRouteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;
    private final HubRouteService hubRouteService;

    @PostMapping
    public ResponseEntity<ApiResponse<HubResponse>> createHub(
            @Valid @RequestBody HubCreateRequest request) {
        HubDto dto = hubService.createHub(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(HubResponse.from(dto)));
    }

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HubResponse>>> getAllHubs(
            @RequestParam(required = false) String hubType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,ASC") String sort) {
        int validSize = ALLOWED_SIZES.contains(size) ? size : 10;
        String[] sortParts = sort.split(",");
        Sort sortObj = sortParts.length > 1
                ? Sort.by(Sort.Direction.fromString(sortParts[1].trim()), sortParts[0].trim())
                : Sort.by(sortParts[0].trim());
        Page<HubResponse> result = hubService.getAllHubs(hubType, status, keyword, PageRequest.of(page, validSize, sortObj))
                .map(HubResponse::from);
        return ResponseEntity.ok(ApiResponse.success(new PageResponse<>(result)));
    }

    @GetMapping("/{hub_id}")
    public ResponseEntity<ApiResponse<HubResponse>> getHub(
            @PathVariable UUID hub_id) {
        HubDto dto = hubService.getHub(hub_id);
        return ResponseEntity.ok(ApiResponse.success(HubResponse.from(dto)));
    }

    @PatchMapping("/{hub_id}")
    public ResponseEntity<ApiResponse<HubResponse>> updateHub(
            @PathVariable UUID hub_id,
            @RequestBody HubUpdateRequest request) {
        HubDto dto = hubService.updateHub(hub_id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(HubResponse.from(dto)));
    }

    @DeleteMapping("/{hub_id}")
    public ResponseEntity<ApiResponse<Void>> deleteHub(
            @PathVariable UUID hub_id,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "system") String userName) {
        hubService.deleteHub(hub_id, userName);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{hub_id}/routes")
    public ResponseEntity<ApiResponse<List<HubRouteResponse>>> getRoutesByHub(
            @PathVariable UUID hub_id) {
        List<HubRouteResponse> responses = hubRouteService.getRoutesByHub(hub_id).stream()
                .map(HubRouteResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
