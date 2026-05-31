package com.sparta.deliveryservice.delivery.infrastructure.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackMessageSendRequest {

    private UUID receiverUserId;
    private String receiverSlackId;
    private String messageContent;
    private String referenceType;
}
