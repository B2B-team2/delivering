package com.sparta.companyservice.company.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.internal.CompanyHubMappingRequest;
import com.sparta.common.dto.internal.CompanyHubMappingResponse;
import com.sparta.companyservice.company.application.dto.CompanyHubMappingResult;
import com.sparta.companyservice.company.application.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/internal/companies")
@RequiredArgsConstructor
public class InternalCompanyController {

    private final CompanyService companyService;

    @PostMapping("/hub-mapping")
    public ResponseEntity<ApiResponse<CompanyHubMappingResponse>> getHubMapping(
            @Valid @RequestBody CompanyHubMappingRequest request) {
        
        CompanyHubMappingResult result = companyService.getHubMappings(request.getCompanyIds());

        Map<UUID, CompanyHubMappingResponse.CompanyHubMappingDto> mappingMap = result.getMappings().stream()
                .collect(Collectors.toMap(
                        CompanyHubMappingResult.MappingItem::getCompanyId,
                        item -> CompanyHubMappingResponse.CompanyHubMappingDto.builder()
                                .companyId(item.getCompanyId())
                                .hubId(item.getHubId())
                                .companyName(item.getCompanyName())
                                .build()
                ));

        CompanyHubMappingResponse response = CompanyHubMappingResponse.builder()
                .mappings(mappingMap)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
