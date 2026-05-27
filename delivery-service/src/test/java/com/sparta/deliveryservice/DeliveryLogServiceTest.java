package com.sparta.deliveryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.delivery.global.security.SecurityUtils;
import com.sparta.deliveryservice.deliveryLog.application.service.DeliveryLogService;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLogStatus;
import com.sparta.deliveryservice.deliveryLog.presentation.dto.resqonse.DeliveryLogSearchResponse;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeliveryLogServiceTest {

    @InjectMocks
    private DeliveryLogService deliveryLogService;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private SecurityUtils securityUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = DeliveryLogService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(deliveryLogService, objectMapper);
    }

    @Test
    @DisplayName("배송 로그 페이징 조회 성공 - JSON 파싱 검증")
    void getDeliveryLogs_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        DeliveryLog deliveryLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(routeId)
                .eventType(DeliveryLogStatus.STATUS_CHANGED)
                .previousValue("{\"status\":\"PENDING\"}")
                .currentValue("{\"status\":\"SHIPPED\"}")
                .reason("배송 출고")
                .build();

        try {
            java.lang.reflect.Field idField = DeliveryLog.class.getDeclaredField("logId");
            idField.setAccessible(true);
            idField.set(deliveryLog, logId);
        } catch (Exception e) {
        }

        List<DeliveryLog> logs = List.of(deliveryLog);
        Page<DeliveryLog> mockPage = new PageImpl<>(logs, pageable, logs.size());

        given(deliveryLogRepository.findByDeliveryId(deliveryId, pageable)).willReturn(mockPage);

        given(securityUtils.isMaster()).willReturn(true);

        Page<DeliveryLogSearchResponse.DeliveryLogResponseDto> result = deliveryLogService.getDeliveryLogs(deliveryId, userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        DeliveryLogSearchResponse.DeliveryLogResponseDto dto = result.getContent().get(0);
        assertThat(dto.getLogId()).isEqualTo(logId);
        assertThat(dto.getRouteId()).isEqualTo(routeId);
        assertThat(dto.getEventType()).isEqualTo(DeliveryLogStatus.STATUS_CHANGED);
        assertThat(dto.getReason()).isEqualTo("배송 출고");

        assertThat(dto.getPreviousValue()).isInstanceOf(com.fasterxml.jackson.databind.JsonNode.class);
        assertThat(((com.fasterxml.jackson.databind.JsonNode) dto.getPreviousValue()).get("status").asText()).isEqualTo("PENDING");

        assertThat(dto.getCurrentValue()).isInstanceOf(com.fasterxml.jackson.databind.JsonNode.class);
        assertThat(((com.fasterxml.jackson.databind.JsonNode) dto.getCurrentValue()).get("status").asText()).isEqualTo("SHIPPED");
    }

    @Test
    @DisplayName("배송 로그 페이징 조회 성공 - JSON 파싱 예외 발생 시 문자열로 반환 검증")
    void getDeliveryLogs_ParsingException_ReturnsRawString() {
        UUID deliveryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        DeliveryLog invalidJsonLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(UUID.randomUUID())
                .eventType(DeliveryLogStatus.STATUS_CHANGED)
                .previousValue("잘못된 JSON 형식")
                .currentValue("또 다른 잘못된 형식")
                .reason("테스트")
                .build();

        List<DeliveryLog> logs = List.of(invalidJsonLog);
        Page<DeliveryLog> mockPage = new PageImpl<>(logs, pageable, logs.size());

        given(deliveryLogRepository.findByDeliveryId(deliveryId, pageable)).willReturn(mockPage);

        given(securityUtils.isMaster()).willReturn(true);

        Page<DeliveryLogSearchResponse.DeliveryLogResponseDto> result = deliveryLogService.getDeliveryLogs(deliveryId, userId, pageable);

        assertThat(result).isNotNull();
        DeliveryLogSearchResponse.DeliveryLogResponseDto dto = result.getContent().get(0);

        assertThat(dto.getPreviousValue()).isEqualTo("잘못된 JSON 형식");
        assertThat(dto.getCurrentValue()).isEqualTo("또 다른 잘못된 형식");
    }
}