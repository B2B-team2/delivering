package com.sparta.userservice.user.presentation.dto.request;

import lombok.Getter;

@Getter
public class UserUpdateRequest {

    private String name;
    private String phone;
    private String slackId;
}