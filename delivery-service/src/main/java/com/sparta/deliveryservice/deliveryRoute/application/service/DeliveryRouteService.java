package com.sparta.deliveryservice.deliveryRoute.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryRoute.domin.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.domin.core.RouteStatus;
import com.sparta.deliveryservice.deliveryRoute.domin.repository.DeliveryRouteRepository;
import com.sparta.deliveryservice.deliveryRoute.infrastructure.repository.DeliveryRouteJpaRepository;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteCreateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteStatusUpdateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteCreateResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDetailResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteStatusUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final ObjectMapper objectMapper;

//    @Transactional
//    public DeliveryRouteCreateResponse createDeliveryRoute(DeliveryRouteCreateRequest request) {
//        DeliveryRoute deliveryRoute = DeliveryRoute.builder()
//                .deliveryId(request.getDeliveryId())
//                .sequence(request.getSequence())
//                .fromHubId(request.getFromHubId())
//                .toHubId(request.getToHubId())
//                .estimatedDistance(request.getEstimatedDistance())
//                .estimatedDuration(request.getEstimatedDuration())
//                .status(RouteStatus.PENDING)
//                .build();
//
//
//        DeliveryRoute savedRoute = deliveryRouteRepository.save(deliveryRoute);
//
//        return DeliveryRouteCreateResponse.builder()
//                .routeId(savedRoute.getRouteId())
//                .deliveryId(savedRoute.getDeliveryId())
//                .sequence(savedRoute.getSequence())
//                .fromHubId(savedRoute.getFromHubId())
//                .toHubId(savedRoute.getToHubId())
//                .estimatedDistance(savedRoute.getEstimatedDistance())
//                .estimatedDuration(savedRoute.getEstimatedDuration())
//                .actualDistance(savedRoute.getActualDistance())
//                .actualDuration(savedRoute.getActualDuration())
//                .status(savedRoute.getStatus().name())
//                .createdAt(savedRoute.getCreatedAt())
//                .createdBy(savedRoute.getCreatedBy())
//                .build();
//    }
//
//    public DeliveryRouteDetailResponse getDeliveryDetailRoutes(UUID deliveryId) {
//
//        List<DeliveryRoute> routes = deliveryRouteRepository.findByDeliveryId(deliveryId);
//
//        BigDecimal totalEstimatedDistance = new BigDecimal("412.30");
//        String totalEstimatedDuration = "06:40:00";
//        BigDecimal totalActualDistance = new BigDecimal("87.20");
//        String totalActualDuration = "01:25:00";
//
//        List<DeliveryRouteDetailResponse.DeliveryRouteDetailDto> deliveryRouteDetailDto = routes.stream()
//                .map(route -> DeliveryRouteDetailResponse.DeliveryRouteDetailDto.builder()
//                        .routeId(route.getRouteId())
//                        .sequence(route.getSequence())
//                        .fromHubId(route.getFromHubId())
//                        .fromHubName("임시 허브 센터")
//                        .toHubId(route.getToHubId())
//                        .toHubName("임시 도착 센터")
//                        .estimatedDistance(route.getEstimatedDistance())
//                        .estimatedDuration(route.getEstimatedDuration() != null ? route.getEstimatedDuration().toString() : null)
//                        .actualDistance(route.getActualDistance())
//                        .actualDuration(route.getActualDuration() != null ? route.getActualDuration().toString() : null)
//                        .status(route.getStatus().name())
//                        .build())
//                .collect(Collectors.toList());
//
//        return DeliveryRouteDetailResponse.builder()
//                .deliveryId(deliveryId)
//                .totalEstimatedDistance(totalEstimatedDistance)
//                .totalEstimatedDuration(totalEstimatedDuration)
//                .totalActualDistance(totalActualDistance)
//                .totalActualDuration(totalActualDuration)
//                .routes(deliveryRouteDetailDto)
//                .build();
//    }
//
//    @Transactional
//    public DeliveryRouteStatusUpdateResponse updateRouteStatus(UUID deliveryId, UUID routeId, DeliveryRouteStatusUpdateRequest request, String userId) throws JsonProcessingException {
//        DeliveryRoute route = deliveryRouteRepository.findById(routeId).orElse(null);
//
//        String previousStatus = route != null ? route.getStatus().name() : "PENDING";
//        RouteStatus nextStatus = RouteStatus.valueOf(request.getStatus().toUpperCase());
//
//        String previousValueJson = "";
//        if (route != null) {
//            previousValueJson = objectMapper.writeValueAsString(route);
//        }
//
//        LocalDateTime parsedDuration = null;
//        if (request.getActualDuration() != null) {
//            parsedDuration = LocalDateTime.now();
//        }
//
//        UUID savedLogId = null;
//
//        if (route != null) {
//            route.updateStatusAndActuals(
//                    nextStatus,
//                    request.getActualDistance(),
//                    parsedDuration
//            );
//            route = deliveryRouteRepository.save(route);
//
//            String currentValueJson = objectMapper.writeValueAsString(route);
//
//            DeliveryLog deliveryLog = DeliveryLog.builder()
//                    .deliveryId(deliveryId)
//                    .routeId(routeId)
//                    .eventType("ROUTE_STATUS_UPDATE")
//                    .previousValue(previousValueJson)
//                    .currentValue(currentValueJson)
//                    .reason(request.getReason())
//                    .build();
//
//            DeliveryLog savedLog = deliveryLogRepository.save(deliveryLog);
//            savedLogId = savedLog.getLogId();
//        }
//
//        return DeliveryRouteStatusUpdateResponse.builder()
//                .routeId(route != null ? route.getRouteId() : routeId)
//                .deliveryId(route != null ? route.getDeliveryId() : deliveryId)
//                .sequence(route != null ? route.getSequence() : 0)
//                .previousStatus(previousStatus)
//                .currentStatus(route != null ? route.getStatus().name() : request.getStatus())
//                .estimatedDistance(route != null ? route.getEstimatedDistance() : BigDecimal.ZERO)
//                .estimatedDuration("01:20:00")
//                .actualDistance(route != null ? route.getActualDistance() : request.getActualDistance())
//                .actualDuration(request.getActualDuration())
//                .logId(savedLogId)
//                .updatedAt(LocalDateTime.now())
//                .updatedBy(route != null ? route.getUpdatedBy() : userId)
//                .build();
//    }
}