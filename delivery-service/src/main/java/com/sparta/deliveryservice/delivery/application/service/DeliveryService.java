package com.sparta.deliveryservice.delivery.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryStatus;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.sparta.deliveryservice.delivery.infrastructure.client.CachedHubServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliveryUserServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryHubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryOrderCancelRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryManagerResponse;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryOrderCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryCancelRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryManagerUpdateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryStatusUpdateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryAddressResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryDetailResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryManagerUpdateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliverySearchResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusUpdateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryTrackingResponse;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLogStatus;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRouteStatus;
import com.sparta.deliveryservice.deliveryRoute.domain.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final CachedHubServiceClient cachedHubServiceClient;
    private final DeliveryUserServiceClient deliveryUserServiceClient;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    @Transactional
    public DeliveryCreateResponse createSingleDelivery(DeliveryCreateClientRequest request, String userId) {
        // 1. 중복 체크
        if (deliveryRepository.existsByCompanyOrderId(request.getCompanyOrderId())) {
            throw new IllegalStateException("이미 배송이 생성된 주문건입니다. 주문 ID: " + request.getCompanyOrderId());
        }

        // 2. 배송 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String trackingNumber = "SL" + formatter.format(LocalDateTime.now()) + (int) (Math.random() * 9000 + 1000);

        Delivery delivery = Delivery.builder()
                .companyOrderId(request.getCompanyOrderId())
                .companyReceiveId(request.getCompanyReceiveId())
                .trackingNumber(trackingNumber)
                .status(DeliveryStatus.PENDING)
                .memo(request.getMemo())
                .departureHubId(request.getDepartureHubId())
                .destinationHubId(request.getDestinationHubId())
                .deliveryAddress(request.getDeliveryAddress())
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .postalCode(request.getPostalCode())
                .recipientSlackId(request.getRecipientSlackId())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // 3. 허브 경로 및 매니저 할당
        DeliveryHubRouteSearchRequest hubRequest = DeliveryHubRouteSearchRequest.builder()
                .fromHubId(request.getDepartureHubId())
                .toHubId(request.getDestinationHubId())
                .build();

        DeliveryHubRouteSearchResponse hubClientResponse = cachedHubServiceClient.getHubRouteWithCache(hubRequest);
        DeliveryManagerResponse managerInfo = deliveryUserServiceClient.getManagerInfo(hubClientResponse.getFromHubId());

        savedDelivery.assignDeliveryManager(managerInfo.getDeliveryManagerId(), managerInfo.getDeliverySlackId(), managerInfo.getManagerName(), managerInfo.getManagerPhone());

        // 4. 경로 저장
        List<DeliveryCreateResponse.DeliveryRouteResponseDto> routeResponse = new ArrayList<>();
        String departureHubName = "출발 센터";
        String destinationHubName = "도착 센터";

        if (hubClientResponse != null && hubClientResponse.getRoutes() != null) {
            for (DeliveryHubRouteSearchResponse.HubRouteDto dto : hubClientResponse.getRoutes()) {
                if (dto.getSequence() == 1) departureHubName = dto.getFromHubName();
                if (dto.getSequence() == hubClientResponse.getRoutes().size()) destinationHubName = dto.getToHubName();

                DeliveryRoute route = DeliveryRoute.builder()
                        .deliveryId(savedDelivery.getDeliveryId())
                        .sequence(dto.getSequence())
                        .fromHubId(dto.getFromHubId())
                        .toHubId(dto.getToHubId())
                        .estimatedDistance(dto.getDistance())
                        .estimatedDuration(dto.getDuration())
                        .status(DeliveryRouteStatus.PENDING)
                        .build();

                deliveryRouteRepository.save(route);

                routeResponse.add(DeliveryCreateResponse.DeliveryRouteResponseDto.builder()
                        .routeId(route.getRouteId())
                        .sequence(route.getSequence())
                        .fromHubId(route.getFromHubId())
                        .toHubId(route.getToHubId())
                        .estimatedDistance(route.getEstimatedDistance())
                        .estimatedDuration(route.getEstimatedDuration())
                        .status(route.getStatus().name())
                        .build());
            }
        }

        return DeliveryCreateResponse.builder()
                .deliveryId(savedDelivery.getDeliveryId())
                .companyOrderId(savedDelivery.getCompanyOrderId())
                .trackingNumber(savedDelivery.getTrackingNumber())
                .status(savedDelivery.getStatus().name())
                .departureHubId(savedDelivery.getDepartureHubId())
                .departureHubName(departureHubName)
                .destinationHubId(savedDelivery.getDestinationHubId())
                .destinationHubName(destinationHubName)
                .deliveryAddress(savedDelivery.getDeliveryAddress())
                .recipientName(savedDelivery.getRecipientName())
                .recipientSlackId(savedDelivery.getRecipientSlackId())
                .deliveryManagerId(savedDelivery.getDeliveryManagerId())
                .deliveryManagerName(savedDelivery.getManagerName())
                .deliveryManagerPhone(savedDelivery.getManagerPhone())
                .deliverySlackId(savedDelivery.getDeliverySlackId())
                .memo(savedDelivery.getMemo())
                .routes(routeResponse)
                .build();
    }

    @Transactional
    public List<DeliveryCreateResponse> createDelivery(List<DeliveryCreateClientRequest> requests, String userId) {
        return requests.stream()
                .map(request -> createSingleDelivery(request, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DeliverySearchResponse.DeliveryResponseDto> searchDeliveries(Pageable pageable) {

        Page<Delivery> deliveryPage = deliveryRepository.findAll(pageable);

        return deliveryPage.map(delivery -> DeliverySearchResponse.DeliveryResponseDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus().name())
                .departureHubId(delivery.getDepartureHubId())
                .destinationHubId(delivery.getDestinationHubId())
                .deliveryAddress(delivery.getDeliveryAddress())
                .recipientName(delivery.getRecipientName())
                .deliveryManagerId(delivery.getDeliveryManagerId())
                .finalDispatchDeadlineAt(delivery.getFinalDispatchDeadlineAt())
                .startedAt(delivery.getStartedAt())
                .completedAt(delivery.getCompletedAt())
                .build()
        );
    }

    @Transactional(readOnly = true)
    public DeliveryDetailResponse getDeliveryDetail(UUID deliveryId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송건이 존재하지 않습니다. ID: " + deliveryId));

        List<DeliveryRoute> deliveryRoutes = deliveryRouteRepository.findByDeliveryId(deliveryId);

        DeliveryDetailResponse.DeliveryManagerDto managerDto = DeliveryDetailResponse.DeliveryManagerDto.builder()
                .deliveryManagerId(delivery.getDeliveryManagerId())
                .name(delivery.getManagerName())
                .phone(delivery.getManagerPhone())
                .deliveryManagerSlackId(delivery.getDeliverySlackId())
                .build();

        List<DeliveryDetailResponse.DeliveryRouteDto> route1 = deliveryRoutes.stream()
                .map(route -> DeliveryDetailResponse.DeliveryRouteDto.builder()
                        .routeId(route.getRouteId())
                        .sequence(route.getSequence())
                        .fromHubName(route.getFromHubId().toString())
                        .toHubName(route.getToHubId().toString())
                        .estimatedDistance(route.getEstimatedDistance())
                        .estimatedDuration(route.getEstimatedDuration() != null ? route.getEstimatedDuration().toString() : "00:00:00")
                        .actualDistance(route.getActualDistance())
                        .actualDuration(route.getActualDuration() != null ? route.getActualDuration().toString() : null)
                        .status(route.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return DeliveryDetailResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .companyOrderId(delivery.getCompanyOrderId())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus().name())
                .memo(delivery.getMemo())
                .departureHubId(delivery.getDepartureHubId())
                .departureHubName(delivery.getDepartureHubId().toString())
                .destinationHubId(delivery.getDestinationHubId())
                .destinationHubName(delivery.getDestinationHubId().toString())
                .deliveryAddress(delivery.getDeliveryAddress())
                .recipientName(delivery.getRecipientName())
                .recipientSlackId(delivery.getRecipientSlackId())
                .deliveryManager(managerDto)
                .finalDispatchDeadlineAt(delivery.getFinalDispatchDeadlineAt())
                .startedAt(delivery.getStartedAt())
                .completedAt(delivery.getCompletedAt())
                .routes(route1)
                .build();
    }

    @Transactional(readOnly = true)
    public DeliveryAddressResponse getDeliveryAddress(UUID deliveryId, UUID addressId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송건이 존재하지 않습니다. ID: " + deliveryId));

        return DeliveryAddressResponse.builder()
                .addressId(addressId)
                .companyId(delivery.getCompanyReceiveId())
                .address(delivery.getDeliveryAddress().getAddress())
                .addressDetail(delivery.getDeliveryAddress().getAddressDetail())
                .recipientName(delivery.getRecipientName())
                .phone(delivery.getPhone())
                .postalCode(delivery.getPostalCode())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "deliveryTracking", key = "#trackingNumber", unless = "#result == null")
    public DeliveryTrackingResponse trackDelivery(String trackingNumber) {

        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운송장 번호입니다. 운송장: " + trackingNumber));

        List<DeliveryRoute> deliveryRoutes = deliveryRouteRepository.findByDeliveryId(delivery.getDeliveryId());
        deliveryRoutes.sort(Comparator.comparingInt(DeliveryRoute::getSequence));

        List<DeliveryTrackingResponse.DeliveryRouteOverviewDto> routeOverview = deliveryRoutes.stream()
                .map(route -> DeliveryTrackingResponse.DeliveryRouteOverviewDto.builder()
                        .sequence(route.getSequence())
                        .fromHubName(route.getFromHubId().toString())
                        .toHubName(route.getToHubId().toString())
                        .status(route.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        DeliveryRoute currentRouteEntity = deliveryRoutes.stream()
                .filter(route -> "MOVING".equals(route.getStatus().name()))
                .findFirst()
                .orElse(deliveryRoutes.isEmpty() ? null : deliveryRoutes.get(deliveryRoutes.size() - 1));

        DeliveryTrackingResponse.CurrentLocationDto currentLocationDto = null;
        if (currentRouteEntity != null) {
            currentLocationDto = DeliveryTrackingResponse.CurrentLocationDto.builder()
                    .sequence(currentRouteEntity.getSequence())
                    .fromHubName(currentRouteEntity.getFromHubId().toString())
                    .toHubName(currentRouteEntity.getToHubId().toString())
                    .status(currentRouteEntity.getStatus().name())
                    .build();
        }

        return DeliveryTrackingResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus().name())
                .departureHubName(delivery.getDepartureHubId().toString())
                .destinationHubName(delivery.getDestinationHubId().toString())
                .deliveryAddress(delivery.getDeliveryAddress())
                .recipientName(delivery.getRecipientName())
                .deliveryManagerName(delivery.getDeliveryManagerId().toString())
                .finalDispatchDeadlineAt(delivery.getFinalDispatchDeadlineAt())
                .startedAt(delivery.getStartedAt())
                .completedAt(delivery.getCompletedAt())
                .currentLocation(currentLocationDto)
                .routes(routeOverview)
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", key = "#result.trackingNumber")
    public DeliveryCancelResponse cancelDelivery(UUID deliveryId, DeliveryCancelRequest request) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송 정보가 존재하지 않습니다. ID: " + deliveryId));

        if (delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소 완료 처리된 배송건입니다.");
        }
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new IllegalStateException("배송이 이미 허브를 출발하여 취소할 수 없는 상태입니다. 현재 상태: " + delivery.getStatus());
        }

        String previousStatusName = delivery.getStatus().name();

        delivery.updateStatus(DeliveryStatus.CANCELLED);

        List<DeliveryRoute> deliveryRoutes = deliveryRouteRepository.findByDeliveryId(deliveryId);
        List<DeliveryCancelResponse.CancelledRouteDto> cancelledRouteDtos = new ArrayList<>();

        for (DeliveryRoute route : deliveryRoutes) {
            route.updateStatus(DeliveryRouteStatus.CANCELLED, null, null);

            cancelledRouteDtos.add(DeliveryCancelResponse.CancelledRouteDto.builder()
                    .routeId(route.getRouteId())
                    .sequence(route.getSequence())
                    .status(route.getStatus().name())
                    .build());
        }

        String prevJson = "";
        String currJson = "";
        try {
            prevJson = objectMapper.writeValueAsString(Map.of("status", previousStatusName));
            currJson = objectMapper.writeValueAsString(Map.of("status", "CANCELLED"));
        } catch (Exception e) {
            prevJson = "{\"status\":\"" + previousStatusName + "\"}";
            currJson = "{\"status\":\"CANCELLED\"}";
        }

        DeliveryLog cancelLog = DeliveryLog.builder()
                .deliveryId(delivery.getDeliveryId())
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.CANCELLED)
                .previousValue(prevJson)
                .currentValue(currJson)
                .reason(request.getReason())
                .build();

        DeliveryLog savedLog = deliveryLogRepository.save(cancelLog);

        return DeliveryCancelResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .trackingNumber(delivery.getTrackingNumber())
                .previousStatus(previousStatusName)
                .currentStatus("CANCELLED")
                .cancelledRoutes(cancelledRouteDtos)
                .logId(savedLog.getLogId())
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", key = "#result.trackingNumber")
    public DeliveryStatusUpdateResponse updateDeliveryStatus(UUID deliveryId, DeliveryStatusUpdateRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송 정보가 존재하지 않습니다. ID: " + deliveryId));

        String previousStatusName = delivery.getStatus().name();

        delivery.updateStatus(request.getStatus());


        String prevJson = "";
        String currJson = "";
        try {
            prevJson = objectMapper.writeValueAsString(Map.of("status", previousStatusName));
            currJson = objectMapper.writeValueAsString(Map.of("status", request.getStatus()));
        } catch (Exception e) {
            prevJson = "{\"previousStatus\":\"" + previousStatusName + "\"}";
            currJson = "{\"currentStatus\":\"" + request.getStatus() + "\"}";
        }

        DeliveryLog cancelLog = DeliveryLog.builder()
                .deliveryId(delivery.getDeliveryId())
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.STATUS_CHANGED)
                .previousValue(prevJson)
                .currentValue(currJson)
                .reason(request.getReason())
                .build();

        DeliveryLog savedLog = deliveryLogRepository.save(cancelLog);

        return DeliveryStatusUpdateResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .previousStatus(prevJson)
                .currentStatus(currJson)
                .startedAt(delivery.getStartedAt())
                .completedAt(delivery.getCompletedAt())
                .logId(savedLog.getLogId())
                .updatedAt(delivery.getUpdatedAt())
                .updatedBy(delivery.getUpdatedBy())
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", key = "#result.trackingNumber")
    public DeliveryManagerUpdateResponse updateDeliveryManager(UUID deliveryId, DeliveryManagerUpdateRequest request) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송 정보가 존재하지 않습니다. ID: " + deliveryId));

        UUID previousManagerId = delivery.getDeliveryManagerId();
        String previousManagerSlackId = delivery.getDeliverySlackId();
        String previousManagerName = delivery.getManagerName();
        String previousManagerPhone = delivery.getManagerPhone();

        delivery.updateDeliveryManager(request.getDeliveryManagerId(), request.getDeliveryManagerSlackId(), request.getName(), request.getPhone());

        String prevJson = "";
        String currJson = "";
        try {
            prevJson = objectMapper.writeValueAsString(Map.of(
                    "deliveryManagerId", previousManagerId != null ? previousManagerId.toString() : "",
                    "deliveryManagerSlackId", previousManagerSlackId != null ? previousManagerSlackId.toString() : "",
                    "name", previousManagerName,
                    "phone", previousManagerPhone
            ));

            currJson = objectMapper.writeValueAsString(Map.of(
                    "deliveryManagerId", request.getDeliveryManagerId().toString(),
                    "deliveryManagerSlackId", request.getDeliveryManagerSlackId().toString(),
                    "name", request.getName(),
                    "phone", request.getPhone()
            ));
        } catch (Exception e) {
            prevJson = "{\"deliveryManagerId\":\"" + previousManagerId + "\"}";
            currJson = "{\"deliveryManagerId\":\"" + request.getDeliveryManagerId() + "\"}";
        }

        DeliveryLog managerChangedLog = DeliveryLog.builder()
                .deliveryId(delivery.getDeliveryId())
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.MANAGER_CHANGED)
                .previousValue(prevJson)
                .currentValue(currJson)
                .reason(request.getReason())
                .build();

        DeliveryLog savedLog = deliveryLogRepository.save(managerChangedLog);

        DeliveryManagerUpdateResponse.PreviousManagerDto prevDto = DeliveryManagerUpdateResponse.PreviousManagerDto.builder()
                .deliveryManagerId(previousManagerId)
                .name(previousManagerName)
                .phone(previousManagerPhone)
                .ManagerSlackId(previousManagerSlackId)
                .build();

        DeliveryManagerUpdateResponse.CurrentManagerDto currDto = DeliveryManagerUpdateResponse.CurrentManagerDto.builder()
                .deliveryManagerId(delivery.getDeliveryManagerId())
                .name(request.getName())
                .phone(request.getPhone())
                .ManagerSlackId(request.getDeliveryManagerSlackId())
                .build();

        return DeliveryManagerUpdateResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .previousManager(prevDto)
                .currentManager(currDto)
                .logId(savedLog.getLogId())
                .updatedAt(delivery.getUpdatedAt())
                .updatedBy(delivery.getUpdatedBy())
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", key = "#trackingNumber")
    public DeliveryStatusResponse startDelivery(String trackingNumber) {

        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운송장 번호입니다. 운송장: " + trackingNumber));

        delivery.startDelivery(trackingNumber);

        DeliveryAddress address = delivery.getDeliveryAddress();
        String flatAddress = address.getAddress();
        String flatAddressDetail = address.getAddressDetail();

        return DeliveryStatusResponse.builder()
                .deliveryManagerId(delivery.getDeliveryManagerId())
                .deliveryManagerName(delivery.getManagerName())
                .deliveryManagerPhone(delivery.getPhone())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus().name())
                .startedAt(delivery.getStartedAt())
                .address(flatAddress)
                .addressDetail(flatAddressDetail)
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", key = "#trackingNumber")
    public DeliveryStatusResponse completeDelivery(String trackingNumber) {

        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운송장 번호입니다. 운송장: " + trackingNumber));

        delivery.completeDelivery(trackingNumber);

        DeliveryAddress addressObj = delivery.getDeliveryAddress();
        String flatAddress = (addressObj != null) ? addressObj.getAddress() : null;
        String flatAddressDetail = (addressObj != null) ? addressObj.getAddressDetail() : null;

        return DeliveryStatusResponse.builder()
                .deliveryManagerId(delivery.getDeliveryManagerId())
                .deliveryManagerName(delivery.getManagerName())
                .deliveryManagerPhone(delivery.getPhone())
                .deliveryManagerSlackId(delivery.getDeliverySlackId())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus().name())
                .startedAt(delivery.getStartedAt())
                .address(flatAddress)
                .addressDetail(flatAddressDetail)
                .build();
    }

    @Transactional
    @CacheEvict(value = "deliveryTracking", allEntries = true)
    public DeliveryOrderCancelResponse cancelDeliveriesByOrderId(List<DeliveryOrderCancelRequest> requests) {

        List<UUID> orderIds = requests.stream()
                .map(DeliveryOrderCancelRequest::getOrderId)
                .collect(Collectors.toList());

        List<DeliveryOrderCancelResponse.CancelledDeliveryDto> cancelledDeliveries = new ArrayList<>();
        int cancelledCount = 0;
        int skippedCount = 0;

        org.springframework.cache.Cache trackingCache = cacheManager.getCache("deliveryTracking");
        for (UUID orderId : orderIds) {
            List<Delivery> deliveries = deliveryRepository.findByCompanyOrderId(orderId);
            for (Delivery delivery : deliveries) {
                if (delivery.getStatus() == DeliveryStatus.CANCELLED) {
                    skippedCount++;
                    continue;
                }

                String previousStatusName = delivery.getStatus().name();
                delivery.updateStatus(DeliveryStatus.CANCELLED);


                if (trackingCache != null) {
                    trackingCache.evict(delivery.getTrackingNumber());
                }

                List<DeliveryRoute> deliveryRoutes = deliveryRouteRepository.findByDeliveryId(delivery.getDeliveryId());

                for (DeliveryRoute route : deliveryRoutes) {
                    String routePreviousStatus = route.getStatus().name(); // 경로의 이전 상태 저장
                    route.updateStatus(DeliveryRouteStatus.CANCELLED, null, null);

                    String routePrevJson = "";
                    String routeCurrJson = "";
                    try {
                        routePrevJson = objectMapper.writeValueAsString(Map.of("status", routePreviousStatus));
                        routeCurrJson = objectMapper.writeValueAsString(Map.of("status", "CANCELLED"));
                    } catch (Exception e) {
                        routePrevJson = "{\"status\":\"" + routePreviousStatus + "\"}";
                        routeCurrJson = "{\"status\":\"CANCELLED\"}";
                    }

                    DeliveryLog routeCancelLog = DeliveryLog.builder()
                            .deliveryId(delivery.getDeliveryId())
                            .routeId(route.getRouteId())
                            .eventType(DeliveryLogStatus.CANCELLED)
                            .previousValue(routePrevJson)
                            .currentValue(routeCurrJson)
                            .reason("주문 일괄 강제 취소에 따른 하위 경로 자동 취소")
                            .build();

                    deliveryLogRepository.save(routeCancelLog);
                }

                cancelledDeliveries.add(DeliveryOrderCancelResponse.CancelledDeliveryDto.builder()
                        .deliveryId(delivery.getDeliveryId())
                        .companyOrderId(delivery.getCompanyOrderId())
                        .previousStatus(previousStatusName)
                        .build());

                cancelledCount++;
            }
        }
        return DeliveryOrderCancelResponse.builder()
                .cancelledCount(cancelledCount)
                .skippedCount(skippedCount)
                .cancelledDeliveries(cancelledDeliveries)
                .build();
    }
}
