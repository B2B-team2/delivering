package com.sparta.userservice.admin.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.userservice.admin.application.service.AdminService;
import com.sparta.userservice.admin.presentation.dto.request.ApprovalRequest;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "전체 사용자 조회")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            Pageable pageable) {
        pageable = PageableUtil.validatePageSize(pageable);
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(name, email, role, pageable)));
    }

    @Operation(summary = "가입 대기 목록 조회")
    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getPendingUsers(Pageable pageable) {
        pageable = PageableUtil.validatePageSize(pageable);
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingUsers(pageable)));
    }

    @Operation(summary = "가입 승인/거절")
    @PatchMapping("/users/{userId}/approval")
    public ResponseEntity<ApiResponse<Void>> processApproval(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID adminId,
            @RequestBody ApprovalRequest request) {
        adminService.processApproval(userId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}