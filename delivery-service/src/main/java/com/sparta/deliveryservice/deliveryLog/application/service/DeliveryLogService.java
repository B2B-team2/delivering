package com.sparta.deliveryservice.deliveryLog.application.service;

import com.sparta.deliveryservice.deliveryLog.infrastructure.persistence.DeliveryLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryLogService {

    private final DeliveryLogJpaRepository deliveryLogJpaRepository;


}
