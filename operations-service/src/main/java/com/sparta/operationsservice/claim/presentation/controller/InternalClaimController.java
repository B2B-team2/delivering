package com.sparta.operationsservice.claim.presentation.controller;

import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/claims")
@RequiredArgsConstructor
public class InternalClaimController {

    private final ClaimService claimService;

    @GetMapping("/{claimId}")
    public ClaimDto getClaim(@PathVariable UUID claimId) {
        return claimService.getClaim(claimId);
    }
}
