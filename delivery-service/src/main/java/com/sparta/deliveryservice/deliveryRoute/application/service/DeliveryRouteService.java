package com.sparta.deliveryservice.deliveryRoute.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.sparta.deliveryservice.delivery.global.security.SecurityUtils;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLogStatus;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRouteStatus;
import com.sparta.deliveryservice.deliveryRoute.domain.repository.DeliveryRouteRepository;
import com.sparta.deliveryservice.deliveryRoute.global.exception.DeliveryRouteErrorCode;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteDeleteRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteStatusUpdateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDeleteResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDetailResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteStatusUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    public DeliveryRouteDetailResponse getDeliveryDetailRoutes(UUID deliveryId) {

        List<DeliveryRoute> routes = deliveryRouteRepository.findByDeliveryId(deliveryId);

        BigDecimal totalEstimatedDistance = new BigDecimal("412.30");
        String totalEstimatedDuration = "06:40:00";
        BigDecimal totalActualDistance = new BigDecimal("87.20");
        String totalActualDuration = "01:25:00";

        List<DeliveryRouteDetailResponse.DeliveryRouteDetailDto> deliveryRouteDetailDto = routes.stream()
                .map(route -> DeliveryRouteDetailResponse.DeliveryRouteDetailDto.builder()
                        .routeId(route.getRouteId())
                        .sequence(route.getSequence())
                        .fromHubId(route.getFromHubId())
                        .fromHubName("임시 허브 센터")
                        .toHubId(route.getToHubId())
                        .toHubName("임시 도착 센터")
                        .estimatedDistance(route.getEstimatedDistance())
                        .estimatedDuration(route.getEstimatedDuration() != null ? route.getEstimatedDuration().toString() : null)
                        .actualDistance(route.getActualDistance())
                        .actualDuration(route.getActualDuration() != null ? route.getActualDuration().toString() : null)
                        .status(route.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return DeliveryRouteDetailResponse.builder()
                .deliveryId(deliveryId)
                .totalEstimatedDistance(totalEstimatedDistance)
                .totalEstimatedDuration(totalEstimatedDuration)
                .totalActualDistance(totalActualDistance)
                .totalActualDuration(totalActualDuration)
                .routes(deliveryRouteDetailDto)
                .build();
    }

    @Transactional
    public DeliveryRouteStatusUpdateResponse updateRouteStatus(UUID deliveryId, UUID routeId, DeliveryRouteStatusUpdateRequest request) throws JsonProcessingException {
        DeliveryRoute route = deliveryRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(DeliveryRouteErrorCode.DELIVERY_ROUTE_NOT_FOUND));

        String previousStatus = route != null ? route.getStatus().name() : "PENDING";
        DeliveryRouteStatus nextStatus = DeliveryRouteStatus.valueOf(request.getStatus().toUpperCase());

        String previousValueJson = "";
        if (route != null) {
            previousValueJson = objectMapper.writeValueAsString(route);
        }

        Time parsedDuration = null;
        if (request.getActualDuration() != null) {
            parsedDuration = Time.valueOf(request.getActualDuration());
        }

        UUID savedLogId = null;

        if (route != null) {
            route.updateStatus(
                    nextStatus,
                    request.getActualDistance(),
                    parsedDuration
            );

            route = deliveryRouteRepository.save(route);

            String currentValueJson = objectMapper.writeValueAsString(route);

            DeliveryLog deliveryLog = DeliveryLog.builder()
                    .deliveryId(deliveryId)
                    .routeId(routeId)
                    .eventType(DeliveryLogStatus.ROUTE_CHANGED)
                    .previousValue(previousValueJson)
                    .currentValue(currentValueJson)
                    .reason(request.getReason())
                    .build();

            DeliveryLog savedLog = deliveryLogRepository.save(deliveryLog);
            savedLogId = savedLog.getLogId();
        }

        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery != null && cacheManager.getCache("deliveryTracking") != null) {
            cacheManager.getCache("deliveryTracking").evict(delivery.getTrackingNumber());
        }

        String estimatedDurationStr = "00:00:00";
        if (route != null && route.getEstimatedDuration() != null) {
            estimatedDurationStr = route.getEstimatedDuration().toLocalTime().toString();
        }

        return DeliveryRouteStatusUpdateResponse.builder()
                .routeId(route != null ? route.getRouteId() : routeId)
                .deliveryId(route != null ? route.getDeliveryId() : deliveryId)
                .sequence(route != null ? route.getSequence() : 0)
                .previousStatus(previousStatus)
                .currentStatus(route != null ? route.getStatus().name() : request.getStatus())
                .estimatedDistance(route != null ? route.getEstimatedDistance() : BigDecimal.ZERO)
                .estimatedDuration(estimatedDurationStr)
                .actualDistance(route != null ? route.getActualDistance() : request.getActualDistance())
                .actualDuration(request.getActualDuration())
                .logId(savedLogId)
                .build();
    }
    @Transactional
    public DeliveryRouteDeleteResponse deleteRoute(UUID deliveryId, UUID routeId, DeliveryRouteDeleteRequest request) throws JsonProcessingException {

        DeliveryRoute route = deliveryRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(DeliveryRouteErrorCode.DELIVERY_ROUTE_NOT_FOUND));

        String previousValueJson = "";
        if (route != null) {
            previousValueJson = objectMapper.writeValueAsString(route);
            route.softDelete(routeId);
            deliveryRouteRepository.save(route);
        }

        UUID savedLogId = null;
        if (route != null) {
            DeliveryLog deliveryLog = DeliveryLog.builder()
                    .deliveryId(deliveryId)
                    .routeId(routeId)
                    .eventType(DeliveryLogStatus.ROUTE_CHANGED)
                    .previousValue(previousValueJson)
                    .currentValue("DELETE")
                    .reason(request.getReason())
                    .build();

            DeliveryLog savedLog = deliveryLogRepository.save(deliveryLog);
            savedLogId = savedLog.getLogId();
        }

        List<DeliveryRoute> remainingList = deliveryRouteRepository.findByDeliveryId(deliveryId);

        List<DeliveryRouteDeleteResponse.RemainingRouteDto> remainingRouteDtos = remainingList.stream()
                .map(remainingRoute -> DeliveryRouteDeleteResponse.RemainingRouteDto.builder()
                        .routeId(remainingRoute.getRouteId())
                        .sequence(remainingRoute.getSequence())
                        .fromHubId(remainingRoute.getFromHubId())
                        .toHubId(remainingRoute.getToHubId())
                        .status(remainingRoute.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery != null && cacheManager.getCache("deliveryTracking") != null) {
            cacheManager.getCache("deliveryTracking").evict(delivery.getTrackingNumber());
        }

        return DeliveryRouteDeleteResponse.builder()
                .deliveryId(deliveryId)
                .deletedRouteId(routeId)
                .remainingRoutes(remainingRouteDtos)
                .logId(savedLogId)
                .deletedAt(LocalDateTime.now())
                .deletedBy(route.getDeletedBy())
                .build();
    }
}