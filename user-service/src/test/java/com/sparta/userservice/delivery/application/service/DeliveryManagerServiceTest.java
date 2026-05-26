package com.sparta.userservice.delivery.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.userservice.delivery.infrastructure.repository.DeliveryManagerRepository;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerAssignRequest;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerStatusRequest;
import com.sparta.userservice.delivery.presentation.dto.response.DeliveryManagerResponse;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.user.domain.entity.DeliveryManager;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import com.sparta.userservice.user.domain.enums.ManagerType;
import com.sparta.userservice.user.domain.enums.Role;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeliveryManagerServiceTest {

    @InjectMocks
    private DeliveryManagerService deliveryManagerService;

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @Mock
    private SecurityUtils securityUtils;

    // ===== getAllDeliveryManagers =====

    @Test
    @DisplayName("배송담당자 목록 조회 - 마스터 성공")
    void getAllDeliveryManagers_master_success() {
        Pageable pageable = PageRequest.of(0, 10);
        DeliveryManager manager = createDeliveryManager(ManagerType.COMPANY_DELIVERY);
        Page<DeliveryManager> page = new PageImpl<>(List.of(manager));

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.searchWithUser(any(), any(), any(), any())).willReturn(page);

        PageResponse<DeliveryManagerResponse> result = deliveryManagerService.getAllDeliveryManagers(
                null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("배송담당자 목록 조회 - 권한 없으면 예외")
    void getAllDeliveryManagers_forbidden() {
        Pageable pageable = PageRequest.of(0, 10);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);

        assertThatThrownBy(() -> deliveryManagerService.getAllDeliveryManagers(null, null, null, pageable))
                .isInstanceOf(BusinessException.class);
    }

    // ===== getDeliveryManager =====

    @Test
    @DisplayName("배송담당자 단건 조회 - 마스터 성공")
    void getDeliveryManager_master_success() {
        UUID userId = UUID.randomUUID();
        DeliveryManager manager = createDeliveryManager(ManagerType.COMPANY_DELIVERY);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.findByUserIdWithUser(userId)).willReturn(Optional.of(manager));

        DeliveryManagerResponse result = deliveryManagerService.getDeliveryManager(userId);

        assertThat(result.getManagerType()).isEqualTo(ManagerType.COMPANY_DELIVERY);
    }

    @Test
    @DisplayName("배송담당자 단건 조회 - 본인 성공")
    void getDeliveryManager_self_success() {
        UUID userId = UUID.randomUUID();
        DeliveryManager manager = createDeliveryManager(ManagerType.COMPANY_DELIVERY);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(userId.toString());
        given(deliveryManagerRepository.findByUserIdWithUser(userId)).willReturn(Optional.of(manager));

        DeliveryManagerResponse result = deliveryManagerService.getDeliveryManager(userId);

        assertThat(result.getManagerType()).isEqualTo(ManagerType.COMPANY_DELIVERY);
    }

    @Test
    @DisplayName("배송담당자 단건 조회 - 타인이면 예외")
    void getDeliveryManager_otherUser_forbidden() {
        UUID userId = UUID.randomUUID();

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(UUID.randomUUID().toString());

        assertThatThrownBy(() -> deliveryManagerService.getDeliveryManager(userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("배송담당자 단건 조회 - 없으면 예외")
    void getDeliveryManager_notFound() {
        UUID userId = UUID.randomUUID();

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.findByUserIdWithUser(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerService.getDeliveryManager(userId))
                .isInstanceOf(BusinessException.class);
    }

    // ===== assign =====

    @Test
    @DisplayName("배송담당자 배정 - 마스터 성공")
    void assign_master_success() {
        DeliveryManagerAssignRequest request = createAssignRequest(ManagerType.COMPANY_DELIVERY);
        DeliveryManager manager = createDeliveryManager(ManagerType.COMPANY_DELIVERY);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.findFirstByManagerTypeAndStatusWithUser(any(), any()))
                .willReturn(Optional.of(manager));

        DeliveryManagerResponse result = deliveryManagerService.assign(request);

        assertThat(result.getManagerType()).isEqualTo(ManagerType.COMPANY_DELIVERY);
    }

    @Test
    @DisplayName("배송담당자 배정 - 권한 없으면 예외")
    void assign_forbidden() {
        DeliveryManagerAssignRequest request = createAssignRequest(ManagerType.COMPANY_DELIVERY);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);

        assertThatThrownBy(() -> deliveryManagerService.assign(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("배송담당자 배정 - 대기 중인 담당자 없으면 예외")
    void assign_noAvailableManager() {
        DeliveryManagerAssignRequest request = createAssignRequest(ManagerType.COMPANY_DELIVERY);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.findFirstByManagerTypeAndStatusWithUser(any(), any()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryManagerService.assign(request))
                .isInstanceOf(BusinessException.class);
    }

    // ===== updateStatus =====

    @Test
    @DisplayName("배송담당자 상태 변경 - 마스터 성공")
    void updateStatus_master_success() {
        UUID userId = UUID.randomUUID();
        DeliveryManager manager = createDeliveryManager(ManagerType.COMPANY_DELIVERY);
        DeliveryManagerStatusRequest request = createStatusRequest(DeliveryManagerStatus.DELIVERING);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(deliveryManagerRepository.findByUserIdWithUser(userId)).willReturn(Optional.of(manager));

        DeliveryManagerResponse result = deliveryManagerService.updateStatus(userId, request);

        assertThat(result.getStatus()).isEqualTo(DeliveryManagerStatus.DELIVERING);
    }

    @Test
    @DisplayName("배송담당자 상태 변경 - 권한 없으면 예외")
    void updateStatus_forbidden() {
        UUID userId = UUID.randomUUID();
        DeliveryManagerStatusRequest request = createStatusRequest(DeliveryManagerStatus.DELIVERING);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);

        assertThatThrownBy(() -> deliveryManagerService.updateStatus(userId, request))
                .isInstanceOf(BusinessException.class);
    }

    // ===== 헬퍼 =====

    private DeliveryManager createDeliveryManager(ManagerType managerType) {
        User user = User.create("test@test.com", "KEYCLOAK_MANAGED", "테스트", "010-1234-5678", "U123", Role.COMPANY_DELIVERY_MANAGER);
        user.approve(UUID.randomUUID());
        return DeliveryManager.create(user, managerType, 0);
    }

    private DeliveryManagerAssignRequest createAssignRequest(ManagerType managerType) {
        DeliveryManagerAssignRequest request = new DeliveryManagerAssignRequest();
        ReflectionTestUtils.setField(request, "managerType", managerType);
        return request;
    }

    private DeliveryManagerStatusRequest createStatusRequest(DeliveryManagerStatus status) {
        DeliveryManagerStatusRequest request = new DeliveryManagerStatusRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }
}