package com.sparta.deliveryservice.delivery.application.service;

import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliverySlackServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.SlackMessageSendRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryAiResponse;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverySlackNotificationService {

    private final DeliverySlackServiceClient deliverySlackServiceClient;

    @Async
    public void sendSlackNotificationAsync(Delivery delivery, DeliveryCreateClientRequest request) {

        String content = String.format(
                "주문 번호 : %s\n" +
                        "주문자 정보 : %s\n" +
                        "주문 시간 : %s\n" +
                        "상품 정보 : %s\n" +
                        "요청 사항 : %s\n" +
                        "발송지 : %s\n" +
                        "도착지 : %s\n" +
                        "배송담당자 : %s / %s" +
                        "도착 예정 시간 : %s\n",
                delivery.getCompanyOrderId(),
                request.getRecipientName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                request.getMemo(),
                delivery.getDepartureHubId().toString(),
                delivery.getDeliveryAddress().toString(),
                delivery.getManagerName(), delivery.getManagerPhone(),
                delivery.getFinalDispatchDeadlineAt()
        );

        SlackMessageSendRequest slackRequest = SlackMessageSendRequest.builder()
                .receiverSlackId(delivery.getDeliverySlackId())
                .receiverUserId(request.getCompanyOrderId())
                .messageContent(content)
                .referenceType("DELIVERY_NOTIFICATION")
                .build();

        deliverySlackServiceClient.sendSlackMessage(slackRequest);
    }
}