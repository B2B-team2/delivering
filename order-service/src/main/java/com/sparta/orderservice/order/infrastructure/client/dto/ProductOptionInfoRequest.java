package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

// Product Service: 상품 옵션 일괄 조회 요청
public record ProductOptionInfoRequest(
        List<UUID> productOptionIds
) {}
