package com.sparta.hubservice.hub.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.hub.infrastructure.feign.CompanyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs/test")
@RequiredArgsConstructor
public class HubTestController {

    private final CompanyClient companyClient;

    @GetMapping("/company-exists")
    public ResponseEntity<ApiResponse<Boolean>> testCompanyExists(@RequestParam UUID hubId) {
        return companyClient.existsCompanyInHub(hubId);
    }
}
