package com.sparta.userservice.admin.presentation.dto.request;

import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import lombok.Getter;

@Getter
public class ApprovalRequest {

    private ApprovalStatus status;
    private String rejectedReason;
}