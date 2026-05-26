package com.sparta.deliveryservice.deliveryLog.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
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
    private final ObjectMapper objectMapper; // JSON 이력 데이터를 오가는 오브젝트 파서 주입

    @Transactional(readOnly = true)
    public Page<DeliveryLogSearchResponse.DeliveryLogResponseDto> getDeliveryLogs(UUID deliveryId, Pageable pageable) {

        Page<DeliveryLog> logPage = deliveryLogRepository.findByDeliveryId(deliveryId, pageable);

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