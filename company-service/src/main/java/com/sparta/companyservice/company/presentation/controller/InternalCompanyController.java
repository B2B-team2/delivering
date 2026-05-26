package com.sparta.companyservice.company.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyHubMappingRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyHubMappingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/companies")
@RequiredArgsConstructor
public class InternalCompanyController {

    private final CompanyService companyService;

    @PostMapping("/hub-mapping")
    public ApiResponse<CompanyHubMappingResponse> getHubMapping(
            @Valid @RequestBody CompanyHubMappingRequest request) {
        
        CompanyHubMappingResponse response = CompanyHubMappingResponse.from(
                companyService.getHubMappings(request.getCompanyIds()));

        return ApiResponse.success(response);
    }

    @GetMapping("/exists")
    public ApiResponse<Boolean> existsCompanyInHub(@RequestParam UUID hubId) {
        return ApiResponse.success(companyService.existsCompanyInHub(hubId));
    }
}
