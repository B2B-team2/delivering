package com.sparta.companyservice.company.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.service.CompanyAddressService;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressDeleteResponse;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressResponse;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressUpdateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyResponse;
import com.sparta.companyservice.company.presentation.dto.CompanyUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyAddressService companyAddressService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@RequestBody @Valid CompanyCreateRequest request) {
        CompanyDto resultDto = companyService.createCompany(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CompanyResponse.from(resultDto)));
    }

    @PostMapping("/{companyId}/addresses")
    public ResponseEntity<ApiResponse<CompanyAddressResponse>> createAddress(
            @PathVariable UUID companyId,
            @RequestBody @Valid CompanyAddressCreateRequest request) {
        CompanyAddressDto resultDto = companyAddressService.registerAddress(companyId, request.toCommand(companyId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CompanyAddressResponse.from(resultDto)));
    }

    @GetMapping("/{companyId}/addresses")
    public ResponseEntity<ApiResponse<PageResponse<CompanyAddressResponse>>> getAddresses(
            @PathVariable UUID companyId,
            @PageableDefault(size = 10, sort = {"isDefault", "createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<CompanyAddressResponse> response = new PageResponse<>(
                companyAddressService.getAddresses(companyId, validatedPageable).map(CompanyAddressResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CompanyAddressDeleteResponse>> deleteAddress(
            @PathVariable UUID addressId) {
        // TODO: 권한 로직 및 실제 사용자 정보 연동 시 수정 필요 (시스템 UUID 고정값 교체)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CompanyAddressDto resultDto = companyAddressService.deleteAddress(addressId, userId);
        CompanyAddressDeleteResponse response = CompanyAddressDeleteResponse.of(resultDto.getAddressId(), resultDto.getDeletedAt());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CompanyAddressResponse>> updateAddress(
            @PathVariable UUID addressId,
            @RequestBody @Valid CompanyAddressUpdateRequest request) {
        CompanyAddressDto resultDto = companyAddressService.updateAddress(addressId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(CompanyAddressResponse.from(resultDto)));
    }

    @PatchMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> patchCompany(
            @PathVariable UUID companyId,
            @RequestBody @Valid CompanyUpdateRequest request) {
        CompanyDto resultDto = companyService.updateCompany(companyId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(CompanyResponse.from(resultDto)));
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

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable UUID companyId) {
        CompanyDto resultDto = companyService.getCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success(CompanyResponse.from(resultDto)));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> deleteCompany(
            @PathVariable UUID companyId) {
        // TODO: 추후 인증/인가 로직 도입 시 실제 사용자 ID로 교체 필요
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CompanyDto resultDto = companyService.deleteCompany(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(CompanyResponse.from(resultDto)));
    }
}
