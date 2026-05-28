package com.sparta.deliveryservice;

import com.sparta.common.dto.BusinessException;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryStatus;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.sparta.deliveryservice.delivery.global.exception.DeliveryErrorCode;
import com.sparta.deliveryservice.delivery.global.security.SecurityUtils;
import com.sparta.deliveryservice.delivery.infrastructure.client.CachedHubServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliveryOrderServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliveryUserServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryHubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryManagerResponse;
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
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.sparta.common.architecture.BaseArchitectureTest.domain_prefix_naming_rule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTests {

    @ArchTest
    public static final ArchRule 배달_도메인_네이밍_규칙 = domain_prefix_naming_rule("Delivery");

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private CachedHubServiceClient cachedHubServiceClient;

    @Mock
    private DeliveryUserServiceClient deliveryUserServiceClient;

    @Mock
    private DeliveryOrderServiceClient deliveryOrderServiceClient;

    @Mock
    private SecurityUtils securityUtils;

    @Test
    @DisplayName("배송 생성 성공 - 단일 건 생성 검증")
    void createSingleDelivery_Success() {
        // Given
        UUID companyOrderId = UUID.randomUUID();
        
        UUID departureHubId = UUID.randomUUID();
        UUID destinationHubId = UUID.randomUUID();

        DeliveryCreateClientRequest requestDto = DeliveryCreateClientRequest.builder()
                .companyOrderId(companyOrderId)
                .departureHubId(departureHubId)
                .destinationHubId(destinationHubId)
                .deliveryAddress(new DeliveryAddress("강원도 원주시", "102호"))
                .recipientName("홍길동")
                .recipientSlackId("slack_hong")
                .build();

        // 저장될 Mock Delivery 설정
        Delivery mockDelivery = Delivery.builder()
                .companyOrderId(companyOrderId)
                .departureHubId(departureHubId)
                .destinationHubId(destinationHubId)
                .status(DeliveryStatus.PENDING)
                .build();

        // Reflection으로 deliveryId 주입
        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(mockDelivery, UUID.randomUUID());
        } catch (Exception e) { }

        given(deliveryRepository.existsByCompanyOrderId(companyOrderId)).willReturn(false);
        given(deliveryRepository.save(any(Delivery.class))).willReturn(mockDelivery);

        // Hub 및 Manager API Mocking
        DeliveryHubRouteSearchResponse.HubRouteDto routeDto = DeliveryHubRouteSearchResponse.HubRouteDto.builder()
                .sequence(1)
                .fromHubId(departureHubId) // 이 값이 전달됨
                .toHubId(destinationHubId)
                .fromHubName("서울 허브")
                .toHubName("원주 허브")
                .build();


        DeliveryHubRouteSearchResponse mockHubResponse = DeliveryHubRouteSearchResponse.builder()
                .fromHubId(departureHubId) // <--- 이 값이 중요합니다!
                .routes(List.of(routeDto))
                .build();

        given(cachedHubServiceClient.getHubRouteWithCache(any(DeliveryHubRouteSearchRequest.class)))
                .willReturn(mockHubResponse);

        given(deliveryUserServiceClient.getManagerInfo(departureHubId))
                .willReturn(DeliveryManagerResponse.builder().deliveryManagerId(UUID.randomUUID()).build());
        DeliveryCreateResponse actualResponse = deliveryService.createSingleDelivery(requestDto);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCompanyOrderId()).isEqualTo(companyOrderId);
        assertThat(actualResponse.getDepartureHubName()).isEqualTo("서울 허브");
        assertThat(actualResponse.getDestinationHubName()).isEqualTo("원주 허브");
        assertThat(actualResponse.getRoutes()).hasSize(1);
    }

    @Test
    @DisplayName("배송 생성 실패 - 이미 배송이 생성된 주문 ID인 경우 중복 에러 반환")
    void createInternalDeliveries_DuplicateOrder_ThrowsException() {
        // Given
        UUID companyOrderId = UUID.randomUUID();
         // String에서 UUID로 변경
        DeliveryCreateClientRequest requestDto = DeliveryCreateClientRequest.builder()
                .companyOrderId(companyOrderId)
                .build();

        given(deliveryRepository.existsByCompanyOrderId(companyOrderId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> deliveryService.createSingleDelivery(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DUPLICATE_DELIVERY);
    }

    @Test
    @DisplayName("배송 목록 페이징 조회 성공")
    void searchDeliveries_Success() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        UUID deliveryId1 = UUID.randomUUID();
        UUID deliveryId2 = UUID.randomUUID();
        

        Delivery delivery1 = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.PENDING)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("서울시 강남구", "101호"))
                .recipientName("이순신")
                .deliveryManagerId(UUID.randomUUID())
                .build();

        Delivery delivery2 = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000002")
                .status(DeliveryStatus.SHIPPED)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("부산시 해운대구", "202호"))
                .recipientName("강감찬")
                .deliveryManagerId(UUID.randomUUID())
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery1, deliveryId1);
            idField.set(delivery2, deliveryId2);
        } catch (Exception e) {
        }

        List<Delivery> deliveries = List.of(delivery1, delivery2);
        org.springframework.data.domain.Page<Delivery> mockPage = new org.springframework.data.domain.PageImpl<>(deliveries, pageable, deliveries.size());

        given(deliveryRepository.findAll(pageable)).willReturn(mockPage);

        org.springframework.data.domain.Page<DeliverySearchResponse.DeliveryResponseDto> result = deliveryService.searchDeliveries(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        DeliverySearchResponse.DeliveryResponseDto firstDto = result.getContent().get(0);
        assertThat(firstDto.getDeliveryId()).isEqualTo(deliveryId1);
        assertThat(firstDto.getTrackingNumber()).isEqualTo("SL2605251200000001");
        assertThat(firstDto.getStatus()).isEqualTo("PENDING");
        assertThat(firstDto.getRecipientName()).isEqualTo("이순신");

        DeliverySearchResponse.DeliveryResponseDto secondDto = result.getContent().get(1);
        assertThat(secondDto.getDeliveryId()).isEqualTo(deliveryId2);
        assertThat(secondDto.getTrackingNumber()).isEqualTo("SL2605251200000002");
        assertThat(secondDto.getStatus()).isEqualTo("SHIPPED");
        assertThat(secondDto.getRecipientName()).isEqualTo("강감찬");
    }

    @Test
    @DisplayName("배송 상세 조회 성공")
    void getDeliveryDetail_Success() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        String managerSlackId = "SlackId";
        UUID departureHubId = UUID.randomUUID();
        UUID destinationHubId = UUID.randomUUID();
        

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.PENDING)
                .memo("경비실에 맡겨주세요.")
                .departureHubId(departureHubId)
                .destinationHubId(destinationHubId)
                .deliveryAddress(new DeliveryAddress("경기도 성남시", "303호"))
                .recipientName("홍길동")
                .recipientSlackId("slack_hong")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);

            delivery.assignDeliveryManager(managerId, managerSlackId, "김매니저", "010-1111-2222");
        } catch (Exception e) {
        }

        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .fromHubId(departureHubId)
                .toHubId(destinationHubId)
                .estimatedDistance(new java.math.BigDecimal("45.20"))
                .estimatedDuration(java.sql.Time.valueOf("01:15:00"))
                .status(DeliveryRouteStatus.PENDING)
                .build();

        try {
            java.lang.reflect.Field routeIdField = DeliveryRoute.class.getDeclaredField("routeId");
            routeIdField.setAccessible(true);
            routeIdField.set(route, routeId);
        } catch (Exception e) {
        }

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));
        given(deliveryRouteRepository.findByDeliveryId(deliveryId)).willReturn(List.of(route));

        DeliveryDetailResponse result = deliveryService.getDeliveryDetail(deliveryId);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getTrackingNumber()).isEqualTo("SL2605251200000001");
        assertThat(result.getDeliveryManager().getDeliveryManagerId()).isEqualTo(managerId);
        assertThat(result.getDeliveryManager().getName()).isEqualTo("김매니저");

        assertThat(result.getRoutes()).hasSize(1);
        assertThat(result.getRoutes().get(0).getRouteId()).isEqualTo(routeId);
        assertThat(result.getRoutes().get(0).getFromHubName()).isEqualTo(departureHubId.toString());
        assertThat(result.getRoutes().get(0).getEstimatedDuration()).isEqualTo("01:15:00");
    }

    @Test
    @DisplayName("배송 상세 조회 실패 - 존재하지 않는 배송 ID")
    void getDeliveryDetail_NotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        

        given(deliveryRepository.findById(nonExistentId)).willReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deliveryService.getDeliveryDetail(nonExistentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);// 에러 코드로 검증
    }

    @Test
    @DisplayName("배송지 주소 조회 성공")
    void getDeliveryAddress_Success() {
        UUID deliveryId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID companyReceiveId = UUID.randomUUID();
        

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(companyReceiveId)
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.PENDING)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("서울특별시 관악구", "501호"))
                .recipientName("강감찬")
                .phone("010-5555-6666")
                .postalCode("08825")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);
        } catch (Exception e) {
        }

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));

        DeliveryAddressResponse result = deliveryService.getDeliveryAddress(deliveryId, addressId);

        assertThat(result).isNotNull();
        assertThat(result.getAddressId()).isEqualTo(addressId);
        assertThat(result.getCompanyId()).isEqualTo(companyReceiveId);
        assertThat(result.getAddress()).isEqualTo("서울특별시 관악구");
        assertThat(result.getAddressDetail()).isEqualTo("501호");
        assertThat(result.getRecipientName()).isEqualTo("강감찬");
        assertThat(result.getPhone()).isEqualTo("010-5555-6666");
        assertThat(result.getPostalCode()).isEqualTo("08825");
    }

    @Test
    @DisplayName("배송지 주소 조회 실패 - 존재하지 않는 배송 ID")
    void getDeliveryAddress_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        
        given(deliveryRepository.findById(nonExistentId)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> deliveryService.getDeliveryAddress(nonExistentId, addressId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    @DisplayName("운송장 번호로 배송 추적 성공")
    void trackDelivery_Success() {
        String trackingNumber = "SL2605251200000001";
        UUID deliveryId = UUID.randomUUID();
        UUID departureHubId = UUID.randomUUID();
        UUID destinationHubId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber(trackingNumber)
                .status(DeliveryStatus.SHIPPED)
                .departureHubId(departureHubId)
                .destinationHubId(destinationHubId)
                .deliveryAddress(new DeliveryAddress("강원도 원주시", "707호"))
                .recipientName("홍길동")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);

            java.lang.reflect.Field managerIdField = Delivery.class.getDeclaredField("deliveryManagerId");
            managerIdField.setAccessible(true);
            managerIdField.set(delivery, managerId);
        } catch (Exception e) {
        }

        DeliveryRoute route1 = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .fromHubId(departureHubId)
                .toHubId(destinationHubId)
                .status(DeliveryRouteStatus.MOVING)
                .build();

        DeliveryRoute route2 = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(2)
                .fromHubId(destinationHubId)
                .toHubId(UUID.randomUUID())
                .status(DeliveryRouteStatus.PENDING)
                .build();

        List<DeliveryRoute> routes = new java.util.ArrayList<>(List.of(route2, route1));

        given(deliveryRepository.findByTrackingNumber(trackingNumber)).willReturn(java.util.Optional.of(delivery));
        given(deliveryRouteRepository.findByDeliveryId(deliveryId)).willReturn(routes);

        DeliveryTrackingResponse result = deliveryService.trackDelivery(trackingNumber);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getTrackingNumber()).isEqualTo(trackingNumber);
        assertThat(result.getDeliveryManagerName()).isEqualTo(managerId.toString());

        assertThat(result.getRoutes()).hasSize(2);
        assertThat(result.getRoutes().get(0).getSequence()).isEqualTo(1);
        assertThat(result.getRoutes().get(1).getSequence()).isEqualTo(2);

        assertThat(result.getCurrentLocation()).isNotNull();
        assertThat(result.getCurrentLocation().getSequence()).isEqualTo(1);
        assertThat(result.getCurrentLocation().getStatus()).isEqualTo("MOVING");
    }

    @Test
    @DisplayName("배송 추적 실패 - 존재하지 않는 운송장 번호")
    void trackDelivery_NotFound_ThrowsException() {
        
        String invalidTrackingNumber = "INVALID12345";

        given(deliveryRepository.findByTrackingNumber(invalidTrackingNumber)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> deliveryService.trackDelivery(invalidTrackingNumber))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    @DisplayName("배송 취소 성공 - 하위 경로 취소 및 로그 생성 검증")
    void cancelDelivery_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        DeliveryCancelRequest requestDto = DeliveryCancelRequest.builder()
                .reason("고객 요청으로 인한 취소")
                .build();

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.PENDING)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("지정 주소", "상세 주소"))
                .recipientName("홍길동")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);
        } catch (Exception e) {
        }

        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .fromHubId(UUID.randomUUID())
                .toHubId(UUID.randomUUID())
                .status(DeliveryRouteStatus.PENDING)
                .build();

        try {
            java.lang.reflect.Field routeIdField = DeliveryRoute.class.getDeclaredField("routeId");
            routeIdField.setAccessible(true);
            routeIdField.set(route, routeId);
        } catch (Exception e) {
        }

        DeliveryLog mockLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.STATUS_CHANGED)
                .previousValue("{\"status\":\"PENDING\"}")
                .currentValue("{\"status\":\"CANCELLED\"}")
                .reason(requestDto.getReason())
                .build();

        try {
            java.lang.reflect.Field logIdField = DeliveryLog.class.getDeclaredField("logId");
            logIdField.setAccessible(true);
            logIdField.set(mockLog, logId);
        } catch (Exception e) {
        }

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));
        given(deliveryRouteRepository.findByDeliveryId(deliveryId)).willReturn(List.of(route));
        given(deliveryLogRepository.save(any(DeliveryLog.class))).willReturn(mockLog);

        given(securityUtils.isMaster()).willReturn(true);


        DeliveryCancelResponse result = deliveryService.cancelDelivery(deliveryId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getPreviousStatus()).isEqualTo("PENDING");
        assertThat(result.getCurrentStatus()).isEqualTo("CANCELLED");
        assertThat(result.getLogId()).isEqualTo(logId);

        assertThat(result.getCancelledRoutes()).hasSize(1);
        assertThat(result.getCancelledRoutes().get(0).getRouteId()).isEqualTo(routeId);
        assertThat(result.getCancelledRoutes().get(0).getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("배송 취소 실패 - 이미 취소 완료된 상태인 경우")
    void cancelDelivery_AlreadyCancelled_ThrowsException() {
        UUID deliveryId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        
        DeliveryCancelRequest requestDto = DeliveryCancelRequest.builder()
                .reason("고객 요청으로 인한 취소")
                .build();

        Delivery delivery = Delivery.builder()
                .status(DeliveryStatus.CANCELLED)
                .deliveryManagerId(managerId)
                .build();

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));


        // 3. 기대하는 에러 코드를 서비스 코드의 INVALID_STATUS_TRANSITION으로 수정
        assertThatThrownBy(() -> deliveryService.cancelDelivery(deliveryId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    @DisplayName("배송 취소 실패 - 배송이 이미 대기 상태(PENDING)를 벗어난 경우")
    void cancelDelivery_NotPendingStatus_ThrowsException() {
        UUID deliveryId = UUID.randomUUID();
        
        DeliveryCancelRequest requestDto = DeliveryCancelRequest.builder()
                .reason("주소 오 입력")
                .build();

        Delivery delivery = Delivery.builder()
                .status(DeliveryStatus.SHIPPED)
                .build();

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));

        given(securityUtils.isMaster()).willReturn(true);

        assertThatThrownBy(() -> deliveryService.cancelDelivery(deliveryId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    @DisplayName("배송 상태 변경 성공 - 히스토리 로그 적재 검증")
    void updateDeliveryStatus_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        DeliveryStatusUpdateRequest requestDto = DeliveryStatusUpdateRequest.builder()
                .status(DeliveryStatus.DELIVERED)
                .reason("배송 완료 처리")
                .build();

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.SHIPPED)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("지정 주소", "상세 주소"))
                .recipientName("홍길동")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);
        } catch (Exception e) {
        }

        DeliveryLog mockLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.STATUS_CHANGED)
                .previousValue("{\"status\":\"SHIPPED\"}")
                .currentValue("{\"status\":\"DELIVERED\"}")
                .reason(requestDto.getReason())
                .build();

        try {
            java.lang.reflect.Field logIdField = DeliveryLog.class.getDeclaredField("logId");
            logIdField.setAccessible(true);
            logIdField.set(mockLog, logId);
        } catch (Exception e) {
        }

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));
        given(deliveryLogRepository.save(any(DeliveryLog.class))).willReturn(mockLog);

        given(securityUtils.isMaster()).willReturn(true);

        DeliveryStatusUpdateResponse result = deliveryService.updateDeliveryStatus(deliveryId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getPreviousStatus()).contains("SHIPPED");
        assertThat(result.getCurrentStatus()).contains("DELIVERED");
        assertThat(result.getLogId()).isEqualTo(logId);
    }

    @Test
    @DisplayName("배송 상태 변경 실패 - 존재하지 않는 배송 ID")
    void updateDeliveryStatus_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        
        DeliveryStatusUpdateRequest requestDto = DeliveryStatusUpdateRequest.builder()
                .status(DeliveryStatus.DELIVERED)
                .reason("테스트")
                .build();

        given(deliveryRepository.findById(nonExistentId)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(nonExistentId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    @DisplayName("배송 담당자 변경 성공 - 이전/이후 매니저 정보 및 로그 적재 검증")
    void updateDeliveryManager_Success() throws Exception {
        UUID deliveryId = UUID.randomUUID();
        UUID previousManagerId = UUID.randomUUID();
        String previousManagerSlackId = "SlackId12";
        UUID newManagerId = UUID.randomUUID();
        String managerSlackId = "SlackId";
        
        UUID logId = UUID.randomUUID();

        DeliveryManagerUpdateRequest requestDto = DeliveryManagerUpdateRequest.builder()
                .deliveryManagerId(newManagerId)
                .deliveryManagerSlackId(managerSlackId)
                .name("신임매니저")
                .phone("010-8888-9999")
                .reason("담당자 인사이동")
                .build();

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber("SL2605251200000001")
                .status(DeliveryStatus.PENDING)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("테스트 주소", "상세 주소"))
                .recipientName("홍길동")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);

            delivery.assignDeliveryManager(previousManagerId, previousManagerSlackId, "전임매니저", "010-1111-2222");
        } catch (Exception e) {
        }

        DeliveryLog mockLog = DeliveryLog.builder()
                .deliveryId(deliveryId)
                .routeId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .eventType(DeliveryLogStatus.MANAGER_CHANGED)
                .previousValue("{\"name\":\"전임매니저\"}")
                .currentValue("{\"name\":\"신임매니저\"}")
                .reason(requestDto.getReason())
                .build();

        try {
            java.lang.reflect.Field logIdField = DeliveryLog.class.getDeclaredField("logId");
            logIdField.setAccessible(true);
            logIdField.set(mockLog, logId);
        } catch (Exception e) {
        }

        given(deliveryRepository.findById(deliveryId)).willReturn(java.util.Optional.of(delivery));
        given(deliveryLogRepository.save(any(DeliveryLog.class))).willReturn(mockLog);

        given(securityUtils.isMaster()).willReturn(true);

        DeliveryManagerUpdateResponse result = deliveryService.updateDeliveryManager(deliveryId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(result.getLogId()).isEqualTo(logId);

        assertThat(result.getPreviousManager()).isNotNull();
        assertThat(result.getPreviousManager().getDeliveryManagerId()).isEqualTo(previousManagerId);
        assertThat(result.getPreviousManager().getManagerSlackId()).isEqualTo(previousManagerSlackId);
        assertThat(result.getPreviousManager().getName()).isEqualTo("전임매니저");
        assertThat(result.getPreviousManager().getPhone()).isEqualTo("010-1111-2222");

        assertThat(result.getCurrentManager()).isNotNull();
        assertThat(result.getCurrentManager().getDeliveryManagerId()).isEqualTo(newManagerId);
        assertThat(result.getCurrentManager().getManagerSlackId()).isEqualTo(managerSlackId);
        assertThat(result.getCurrentManager().getName()).isEqualTo("신임매니저");
        assertThat(result.getCurrentManager().getPhone()).isEqualTo("010-8888-9999");
    }

    @Test
    @DisplayName("배송 담당자 변경 실패 - 존재하지 않는 배송 ID")
    void updateDeliveryManager_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        
        DeliveryManagerUpdateRequest requestDto = DeliveryManagerUpdateRequest.builder()
                .deliveryManagerId(UUID.randomUUID())
                .name("신임매니저")
                .phone("010-8888-9999")
                .reason("테스트")
                .build();

        given(deliveryRepository.findById(nonExistentId)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> deliveryService.updateDeliveryManager(nonExistentId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    @DisplayName("배송 완료 처리 성공")
    void completeDelivery_Success() {
        String trackingNumber = "SL2605251200000001";
        UUID deliveryId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        String managerSlackId = "SlackId";

        Delivery delivery = Delivery.builder()
                .companyOrderId(UUID.randomUUID())
                .companyReceiveId(UUID.randomUUID())
                .trackingNumber(trackingNumber)
                .status(DeliveryStatus.SHIPPED)
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(new DeliveryAddress("부산시 사하구", "404호"))
                .recipientName("이순신")
                .phone("010-4444-5555")
                .build();

        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("deliveryId");
            idField.setAccessible(true);
            idField.set(delivery, deliveryId);

            java.lang.reflect.Field managerIdField = Delivery.class.getDeclaredField("deliveryManagerId");
            managerIdField.setAccessible(true);
            managerIdField.set(delivery, managerId);

            delivery.assignDeliveryManager(managerId, managerSlackId, "부산매니저", "010-7777-8888");

            java.lang.reflect.Field startedAtField = Delivery.class.getDeclaredField("startedAt");
            startedAtField.setAccessible(true);
            startedAtField.set(delivery, java.time.LocalDateTime.now());
        } catch (Exception e) {
        }

        given(deliveryRepository.findByTrackingNumber(trackingNumber)).willReturn(java.util.Optional.of(delivery));

        given(securityUtils.isMaster()).willReturn(false);
        given(securityUtils.getUserId()).willReturn(managerId);

        doNothing().when(deliveryOrderServiceClient).companyOrderDelivered(any(), any(), any());

        // When
        DeliveryStatusResponse result = deliveryService.completeDelivery(trackingNumber, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrackingNumber()).isEqualTo(trackingNumber);
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(result.getDeliveryManagerId()).isEqualTo(managerId);
        assertThat(result.getDeliveryManagerName()).isEqualTo("부산매니저");
        assertThat(result.getDeliveryManagerPhone()).isEqualTo("010-4444-5555");
        assertThat(result.getAddress()).isEqualTo("부산시 사하구");
        assertThat(result.getAddressDetail()).isEqualTo("404호");
        assertThat(result.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("배송 완료 처리 실패 - 존재하지 않는 운송장 번호")
    void completeDelivery_NotFound_ThrowsException() {
        String invalidTrackingNumber = "INVALID99999";
        UUID userId = UUID.randomUUID();

        given(deliveryRepository.findByTrackingNumber(invalidTrackingNumber)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> deliveryService.completeDelivery(invalidTrackingNumber, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }
}