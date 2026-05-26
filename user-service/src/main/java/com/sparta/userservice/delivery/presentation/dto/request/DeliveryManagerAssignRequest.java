package com.sparta.userservice.delivery.presentation.dto.request;

import com.sparta.userservice.user.domain.enums.ManagerType;
import lombok.Getter;

@Getter
public class DeliveryManagerAssignRequest {
    private ManagerType managerType;
}