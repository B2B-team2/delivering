package com.sparta.deliveryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLogStatus;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryRoute.application.service.DeliveryRouteService;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRouteStatus;
import com.sparta.deliveryservice.deliveryRoute.domain.repository.DeliveryRouteRepository;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteDeleteRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteStatusUpdateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDeleteResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDetailResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteStatusUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeliveryRouteServiceTest {

    @InjectMocks
    private DeliveryRouteService deliveryRouteService;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private CacheManager cacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DeliveryRouteStatus validStatus;
    private String validStatusName;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = DeliveryRouteService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(deliveryRouteService, objectMapper);

        if (DeliveryRouteStatus.values().length > 0) {
            validStatus = DeliveryRouteStatus.values()[0];
            validStatusName = validStatus.name();
        } else {
            validStatusName = "PENDING";
        }
    }

    @Test
    @DisplayName("배송 상세 경로 목록 조회 성공")
    void getDeliveryDetailRoutes_Success() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID fromHubId = UUID.randomUUID();
        UUID toHubId = UUID.randomUUID();

        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .estimatedDistance(new BigDecimal("150.50"))
                .estimatedDuration(Time.valueOf("02:15:00"))
                .status(validStatus)
                .build();

        try {
            java.lang.reflect.Field idField = DeliveryRoute.class.getDeclaredField("routeId");
            idField.setAccessible(true);
            idField.set(route, routeId);

            java.lang.reflect.Field actualDistanceField = DeliveryRoute.class.getDeclaredField("actualDistance");
            actualDistanceField.setAccessible(true);
            actualDistanceField.set(route, new BigDecimal("50.20"));

            java.lang.reflect.Field actualDurationField = DeliveryRoute.class.getDeclaredField("actualDuration");
            actualDurationField.setAccessible(true);
            actualDurationField.set(route, Time.valueOf("00:45:00"));
        } catch (Exception e) {
        }

        given(deliveryRouteRepository.findByDeliveryId(deliveryId)).willReturn(List.of(route));

        DeliveryRouteDetailResponse result = deliveryRouteService.getDeliveryDetailRoutes(deliveryId);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getTotalEstimatedDistance()).isEqualTo(new BigDecimal("412.30"));
        assertThat(result.getTotalEstimatedDuration()).isEqualTo("06:40:00");
        assertThat(result.getTotalActualDistance()).isEqualTo(new BigDecimal("87.20"));
        assertThat(result.getTotalActualDuration()).isEqualTo("01:25:00");

        assertThat(result.getRoutes()).hasSize(1);
        DeliveryRouteDetailResponse.DeliveryRouteDetailDto dto = result.getRoutes().get(0);
        assertThat(dto.getRouteId()).isEqualTo(routeId);
        assertThat(dto.getStatus()).isEqualTo(validStatusName);
    }

    @Test
    @DisplayName("배송 경로 상태 변경 성공 - 히스토리 로그 생성 검증")
    void updateRouteStatus_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        DeliveryRouteStatusUpdateRequest requestDto = DeliveryRouteStatusUpdateRequest.builder()
                .status(validStatusName)
                .actualDistance(new BigDecimal("12.50"))
                .actualDuration("00:30:00")
                .reason("중간 허브 출발")
                .build();

        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(2)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .estimatedDistance(new BigDecimal("15.00"))
                .estimatedDuration(java.sql.Time.valueOf("00:40:00"))
                .status(validStatus)
                .build();

        try {
            java.lang.reflect.Field idField = DeliveryRoute.class.getDeclaredField("routeId");
            idField.setAccessible(true);
            idField.set(route, routeId);
        } catch (Exception e) {
        }

        DeliveryLog mockLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(routeId)
                .eventType(DeliveryLogStatus.ROUTE_CHANGED)
                .reason(requestDto.getReason())
                .build();

        try {
            java.lang.reflect.Field logIdField = DeliveryLog.class.getDeclaredField("logId");
            logIdField.setAccessible(true);
            logIdField.set(mockLog, logId);
        } catch (Exception e) {
        }

        // 🌟 1. 새로 추가한 협력 객체(DeliveryRepository)의 가짜 데이터와 행동 지침 정의
        Delivery mockDelivery = Delivery.builder()
                .trackingNumber("SL2605261512001234")
                .build();
        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(mockDelivery));

        // 🌟 2. 서비스 로직 안에서 cacheManager.getCache() 가 호출될 때 NullPointer 가 나지 않도록 모킹 처리
        org.springframework.cache.Cache mockCache = org.mockito.Mockito.mock(org.springframework.cache.Cache.class);
        given(cacheManager.getCache("deliveryTracking")).willReturn(mockCache);

        // 기존 레포지토리 모킹 유지
        given(deliveryRouteRepository.findById(routeId)).willReturn(java.util.Optional.of(route));
        given(deliveryRouteRepository.save(any(DeliveryRoute.class))).willReturn(route);
        given(deliveryLogRepository.save(any(DeliveryLog.class))).willReturn(mockLog);

        DeliveryRouteStatusUpdateResponse result = deliveryRouteService.updateRouteStatus(deliveryId, routeId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getRouteId()).isEqualTo(routeId);
        assertThat(result.getCurrentStatus()).isEqualTo(validStatusName);
        assertThat(result.getActualDistance()).isEqualTo(new BigDecimal("12.50"));
        assertThat(result.getLogId()).isEqualTo(logId);
    }
    @Test
    @DisplayName("배송 경로 상태 변경 성공 - 경로 정보가 없을 때 기본값 반환 검증")
    void updateRouteStatus_RouteNotFound_ReturnsDefaultValues() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();

        DeliveryRouteStatusUpdateRequest requestDto = DeliveryRouteStatusUpdateRequest.builder()
                .status(validStatusName)
                .actualDistance(new BigDecimal("10.00"))
                .actualDuration("00:20:00")
                .reason("테스트")
                .build();

        given(deliveryRouteRepository.findById(routeId)).willReturn(java.util.Optional.empty());

        DeliveryRouteStatusUpdateResponse result = deliveryRouteService.updateRouteStatus(deliveryId, routeId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getRouteId()).isEqualTo(routeId);
        assertThat(result.getCurrentStatus()).isEqualTo(validStatusName);
        assertThat(result.getEstimatedDistance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getLogId()).isNull();
    }

    @Test
    @DisplayName("배송 경로 논리 삭제 성공 - 남은 경로 목록 반환 검증")
    void deleteRoute_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID remainingRouteId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        DeliveryRouteDeleteRequest requestDto = DeliveryRouteDeleteRequest.builder()
                .reason("경로 오지정으로 인한 취소")
                .build();

        DeliveryRoute targetRoute = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(2)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .status(validStatus)
                .build();

        DeliveryRoute remainingRoute = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .status(validStatus)
                .build();

        try {
            java.lang.reflect.Field idField = DeliveryRoute.class.getDeclaredField("routeId");
            idField.setAccessible(true);
            idField.set(targetRoute, routeId);

            java.lang.reflect.Field remainingIdField = DeliveryRoute.class.getDeclaredField("routeId");
            remainingIdField.setAccessible(true);
            remainingIdField.set(remainingRoute, remainingRouteId);

            java.lang.reflect.Field deletedByField = DeliveryRoute.class.getDeclaredField("deletedBy");
            deletedByField.setAccessible(true);
            deletedByField.set(targetRoute, "ADMIN");
        } catch (Exception e) {
        }

        DeliveryLog mockLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(routeId)
                .eventType(DeliveryLogStatus.ROUTE_CHANGED)
                .reason(requestDto.getReason())
                .build();

        try {
            java.lang.reflect.Field logIdField = DeliveryLog.class.getDeclaredField("logId");
            logIdField.setAccessible(true);
            logIdField.set(mockLog, logId);
        } catch (Exception e) {
        }

        given(deliveryRouteRepository.findById(routeId)).willReturn(java.util.Optional.of(targetRoute));
        given(deliveryRouteRepository.save(any(DeliveryRoute.class))).willReturn(targetRoute);
        given(deliveryLogRepository.save(any(DeliveryLog.class))).willReturn(mockLog);
        given(deliveryRouteRepository.findByDeliveryId(deliveryId)).willReturn(List.of(remainingRoute));

        DeliveryRouteDeleteResponse result = deliveryRouteService.deleteRoute(deliveryId, routeId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getDeletedRouteId()).isEqualTo(routeId);
        assertThat(result.getLogId()).isEqualTo(logId);
        assertThat(result.getDeletedBy()).isEqualTo(targetRoute.getDeletedBy());

        assertThat(result.getRemainingRoutes()).hasSize(1);
        assertThat(result.getRemainingRoutes().get(0).getRouteId()).isEqualTo(remainingRouteId);
    }
}