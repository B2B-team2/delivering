package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.dto.ProductOptionInfo;
import com.sparta.orderservice.global.port.ProductPort;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoItem;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

    private final ProductClient productClient;

    // 상품 옵션 ID 목록 → { productOptionId(UUID): ProductOptionInfo } Map 반환
    // infrastructure DTO(ProductOptionInfoItem)를 application DTO(ProductOptionInfo)로 변환하여 반환
    @CircuitBreaker(name = "productClient", fallbackMethod = "getProductOptionInfosFallback")
    @Override
    public Map<UUID, ProductOptionInfo> getProductOptionInfos(List<UUID> productOptionIds) {
        try {
            Map<String, ProductOptionInfoItem> optionsMap =
                    productClient.getProductOptionInfos(new ProductOptionInfoRequest(productOptionIds))
                            .optionsMap();

            return optionsMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> UUID.fromString(e.getKey()),
                            e -> new ProductOptionInfo(
                                    e.getValue().productOptionId(),
                                    e.getValue().companyId(),
                                    e.getValue().unitPrice()
                            )
                    ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("getProductOptionInfos", e);
        }
    }

    private Map<UUID, ProductOptionInfo> getProductOptionInfosFallback(List<UUID> productOptionIds, Throwable t) {
        if (t instanceof BusinessException be) throw be;
        log.error("[Product][CB] getProductOptionInfos circuit open or timeout: {}", t.getMessage());
        throw new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }

    private RuntimeException handleUnexpectedException(String operation, Exception e) {
        log.error("[Product] Unexpected error [{}]", operation, e);
        return new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}
