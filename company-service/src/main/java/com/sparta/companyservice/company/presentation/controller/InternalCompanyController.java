package com.sparta.companyservice.company.presentation.controller;

import com.sparta.companyservice.company.application.dto.CompanyDefaultAddressDto;
import com.sparta.companyservice.company.application.service.CompanyAddressService;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyHubMappingRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyHubMappingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final CompanyAddressService companyAddressService;

    @PostMapping("/hub-mapping")
    public CompanyHubMappingResponse getHubMapping(
            @Valid @RequestBody CompanyHubMappingRequest request) {
        
        CompanyHubMappingResponse response = CompanyHubMappingResponse.from(
                companyService.getHubMappings(request.getCompanyIds()));

        return response;
    }

    @GetMapping("/exists")
    public Boolean existsCompanyInHub(@RequestParam UUID hubId) {
        return companyService.existsCompanyInHub(hubId);
    }

    @GetMapping("/{companyId}/default-address")
    public CompanyDefaultAddressDto getDefaultAddress(
            @PathVariable("companyId") UUID companyId) {
        return companyAddressService.getDefaultAddress(companyId);
    }
}
