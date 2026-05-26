package com.sparta.operationsservice.claim.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.service.ClaimService;
import com.sparta.operationsservice.claim.presentation.dto.ClaimCreateRequest;
import com.sparta.operationsservice.claim.presentation.dto.ClaimResponse;
import com.sparta.operationsservice.claim.presentation.dto.ClaimStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(
            @RequestBody @Valid ClaimCreateRequest request) {
        ClaimDto resultDto = claimService.createClaim(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ClaimResponse.from(resultDto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<ClaimResponse>>> getClaims(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<ClaimResponse> response = new PageResponse<>(
                claimService.getClaims(validatedPageable).map(ClaimResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaim(
            @PathVariable("claimId") UUID claimId) {
        ClaimDto resultDto = claimService.getClaim(claimId);
        return ResponseEntity.ok(ApiResponse.success(ClaimResponse.from(resultDto)));
    }

    @PatchMapping("/{claimId}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateClaimStatus(
            @PathVariable("claimId") UUID claimId,
            @RequestBody @Valid ClaimStatusUpdateRequest request) {
        ClaimDto resultDto = claimService.updateClaimStatus(claimId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ClaimResponse.from(resultDto)));
    }
}
