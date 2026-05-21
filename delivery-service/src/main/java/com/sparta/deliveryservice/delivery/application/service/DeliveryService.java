package com.sparta.deliveryservice.delivery.application.service;

import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.infrastructure.persistence.DeliveryJpaRepository;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryJpaRepository deliveryJpaRepository;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryCreateRequest deliveryCreateRequest) {

    }
}
