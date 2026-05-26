package com.sparta.userservice.user.presentation.dto.response;

import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.Role;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID userId;
    private final String email;
    private final String name;
    private final String phone;
    private final String slackId;
    private final Role role;
    private final ApprovalStatus approvalStatus;

    public UserResponse(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.slackId = user.getSlackId();
        this.role = user.getRole();
        this.approvalStatus = user.getApprovalStatus();
    }
}