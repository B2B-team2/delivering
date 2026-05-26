package com.sparta.userservice.delivery.presentation.dto.response;

import com.sparta.userservice.user.domain.entity.DeliveryManager;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InternalDeliveryManagerResponse {

    private final UUID deliveryManagerId;
    private final String deliveryManagerSlackId;
    private final String managerName;
    private final String managerPhone;

    public InternalDeliveryManagerResponse(DeliveryManager manager) {
        this.deliveryManagerId = manager.getUserId();
        this.deliveryManagerSlackId = manager.getUser().getSlackId();
        this.managerName = manager.getUser().getName();
        this.managerPhone = manager.getUser().getPhone();
    }
}