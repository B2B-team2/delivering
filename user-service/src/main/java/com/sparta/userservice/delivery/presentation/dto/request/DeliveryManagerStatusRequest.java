package com.sparta.userservice.delivery.presentation.dto.request;

import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import lombok.Getter;

@Getter
public class DeliveryManagerStatusRequest {
    private DeliveryManagerStatus status;
}