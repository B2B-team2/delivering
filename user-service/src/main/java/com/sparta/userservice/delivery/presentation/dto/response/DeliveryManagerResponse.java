package com.sparta.userservice.delivery.presentation.dto.response;

import com.sparta.userservice.user.domain.entity.DeliveryManager;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import com.sparta.userservice.user.domain.enums.ManagerType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeliveryManagerResponse {

    private final UUID userId;
    private final String name;
    private final String phone;
    private final String slackId;
    private final ManagerType managerType;
    private final Integer deliveryOrder;
    private final UUID hubId;
    private final String hubName; // 추후 hub-service 연동 시 채울 예정
    private final DeliveryManagerStatus status;
    private final LocalDateTime lastAssignedAt;

    public DeliveryManagerResponse(DeliveryManager manager) {
        this.userId = manager.getUserId();
        this.name = manager.getUser().getName();
        this.phone = manager.getUser().getPhone();
        this.slackId = manager.getUser().getSlackId();
        this.managerType = manager.getManagerType();
        this.deliveryOrder = manager.getDeliveryOrder();
        this.hubId = manager.getHubId();
        this.hubName = null;
        this.status = manager.getStatus();
        this.lastAssignedAt = manager.getLastAssignedAt();
    }
}