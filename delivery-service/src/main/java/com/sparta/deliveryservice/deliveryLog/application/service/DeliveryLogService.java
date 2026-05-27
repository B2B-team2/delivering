package com.sparta.deliveryservice.deliveryLog.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.deliveryservice.delivery.global.security.SecurityUtils;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryLog.global.exception.DeliveryLogErrorCode;
import com.sparta.deliveryservice.deliveryLog.presentation.dto.resqonse.DeliveryLogSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryLogService {

    private final DeliveryLogRepository deliveryLogRepository;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Page<DeliveryLogSearchResponse.DeliveryLogResponseDto> getDeliveryLogs(UUID deliveryId, UUID userId, Pageable pageable) {

        if (!securityUtils.isMaster()) {
            throw new BusinessException(DeliveryLogErrorCode.LOG_ACCESS_DENIED);
        }

        Page<DeliveryLog> logPage = deliveryLogRepository.findByDeliveryId(deliveryId, pageable);

        if (logPage.isEmpty()) {
            throw new BusinessException(DeliveryLogErrorCode.DELIVERY_LOG_NOT_FOUND);
        }

        return logPage.map(log -> {
            Object parsedPrevious = null;
            Object parsedCurrent = null;

            try {
                if (log.getPreviousValue() != null && !log.getPreviousValue().isBlank()) {
                    parsedPrevious = objectMapper.readTree(log.getPreviousValue());
                }
                if (log.getCurrentValue() != null && !log.getCurrentValue().isBlank()) {
                    parsedCurrent = objectMapper.readTree(log.getCurrentValue());
                }
            } catch (Exception e) {
                parsedPrevious = log.getPreviousValue();
                parsedCurrent = log.getCurrentValue();
            }

            return DeliveryLogSearchResponse.DeliveryLogResponseDto.builder()
                    .logId(log.getLogId())
                    .routeId(log.getRouteId())
                    .eventType(log.getEventType())
                    .previousValue(parsedPrevious)
                    .currentValue(parsedCurrent)
                    .reason(log.getReason())
                    .build();
        });
    }
}