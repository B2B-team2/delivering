package com.sparta.companyservice.company.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(@RequestBody @Valid CompanyCreateRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ApiResponse.created(response);
    }
}
