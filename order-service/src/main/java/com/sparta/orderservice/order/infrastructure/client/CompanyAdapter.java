package com.sparta.orderservice.order.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.port.CompanyPort;
import com.sparta.orderservice.global.dto.DeliveryAddressInfo;
import com.sparta.orderservice.order.infrastructure.client.dto.DefaultDeliveryAddressResponse;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingResponse;
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
public class CompanyAdapter implements CompanyPort {

    private final CompanyClient companyClient;
    private final ObjectMapper objectMapper;

    // companyId 목록을 일괄 조회하여 { companyId → hubId } Map으로 반환
    @CircuitBreaker(name = "companyClient", fallbackMethod = "getHubIdsFallback")
    @Override
    public Map<UUID, UUID> getHubIds(List<UUID> companyIds) {
        try {
            HubMappingResponse response = companyClient.getHubMapping(new HubMappingRequest(companyIds));
            return response.mappings().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> UUID.fromString(e.getKey()),
                            e -> e.getValue().hubId()
                    ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("getHubIds", e);
        }
    }

    // 수령업체의 기본 배송지(is_default=true) 조회
    // address + addressDetail → p_orders.address(jsonb) 형식 JSON으로 조립 후 DeliveryAddressInfo로 반환
    @CircuitBreaker(name = "companyClient", fallbackMethod = "getDefaultDeliveryAddressFallback")
    @Override
    public DeliveryAddressInfo getDefaultDeliveryAddress(UUID receiverCompanyId) {
        try {
            DefaultDeliveryAddressResponse response = companyClient.getDefaultDeliveryAddress(receiverCompanyId);
            String formattedAddress = objectMapper.writeValueAsString(
                    Map.of("address", response.address(), "address_detail", response.addressDetail())
            );
            return new DeliveryAddressInfo(formattedAddress, response.recipientName(), response.phone());
        } catch (JsonProcessingException e) {
            log.error("[Company] Failed to serialize address JSON [getDefaultDeliveryAddress]", e);
            throw new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("getDefaultDeliveryAddress", e);
        }
    }

    private Map<UUID, UUID> getHubIdsFallback(List<UUID> companyIds, Throwable t) {
        log.error("[Company][CB] getHubIds circuit open or timeout: {}", t.getMessage());
        throw new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
    }

    private DeliveryAddressInfo getDefaultDeliveryAddressFallback(UUID receiverCompanyId, Throwable t) {
        log.error("[Company][CB] getDefaultDeliveryAddress circuit open or timeout: {}", t.getMessage());
        throw new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
    }

    private RuntimeException handleUnexpectedException(String operation, Exception e) {
        log.error("[Company] Unexpected error [{}]", operation, e);
        return new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
    }
}
