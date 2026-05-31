package com.sparta.userservice.admin.presentation.dto.request;

import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ApprovalRequest {

    private ApprovalStatus status;
    private String rejectedReason;
    private UUID companyId;
    private UUID hubId;
}