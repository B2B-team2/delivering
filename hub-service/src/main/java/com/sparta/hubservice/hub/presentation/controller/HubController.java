package com.sparta.hubservice.hub.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.hub.application.dto.HubDto;
import com.sparta.hubservice.hub.application.service.HubService;
import com.sparta.hubservice.hub.presentation.dto.HubCreateRequest;
import com.sparta.hubservice.hub.presentation.dto.HubResponse;
import com.sparta.hubservice.hub.presentation.dto.HubUpdateRequest;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @PostMapping
    public ResponseEntity<ApiResponse<HubResponse>> createHub(
            @Valid @RequestBody HubCreateRequest request) {
        HubDto dto = hubService.createHub(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(HubResponse.from(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HubResponse>>> getAllHubs() {
        List<HubResponse> responses = hubService.getAllHubs().stream()
                .map(HubResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
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
}
