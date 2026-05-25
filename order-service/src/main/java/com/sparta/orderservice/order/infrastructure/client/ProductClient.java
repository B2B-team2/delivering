package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionsMapResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    // 상품 옵션 ID 목록 → { productOptionId: (companyId, unitPrice) } 일괄 조회 (draft → order 전환 시 사용)
    @PostMapping("/api/v1/internal/product-options/details")
    ProductOptionsMapResponse getProductOptionInfos(@RequestBody ProductOptionInfoRequest request);
}
