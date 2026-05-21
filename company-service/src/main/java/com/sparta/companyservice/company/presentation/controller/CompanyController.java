package com.sparta.companyservice.company.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@RequestBody @Valid CompanyCreateRequest request) {
        CompanyDto resultDto = companyService.createCompany(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CompanyResponse.from(resultDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> getCompanies(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<CompanyResponse> response = new PageResponse<>(
                companyService.getCompanies(validatedPageable).map(CompanyResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
