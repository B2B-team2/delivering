package com.sparta.userservice.delivery.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.userservice.delivery.infrastructure.repository.DeliveryManagerRepository;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerAssignRequest;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerStatusRequest;
import com.sparta.userservice.delivery.presentation.dto.response.DeliveryManagerResponse;
import com.sparta.userservice.delivery.presentation.dto.response.InternalDeliveryManagerResponse;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.global.exception.UserErrorCode;
import com.sparta.userservice.user.domain.entity.DeliveryManager;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.userservice.user.domain.enums.ManagerType;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final SecurityUtils securityUtils;

    public PageResponse<DeliveryManagerResponse> getAllDeliveryManagers(
            ManagerType managerType, DeliveryManagerStatus status, UUID hubId, Pageable pageable) {
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageResponse<>(
                deliveryManagerRepository.searchWithUser(managerType, status, hubId, sorted)
                        .map(DeliveryManagerResponse::new)
        );
    }

    public DeliveryManagerResponse getDeliveryManager(UUID userId) {
        // MASTER, HUB_MANAGER는 모두 조회 가능, 배송담당자는 본인만
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager() &&
                !securityUtils.getUserId().equals(userId.toString())) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        DeliveryManager manager = deliveryManagerRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.DELIVERY_MANAGER_NOT_FOUND));
        return new DeliveryManagerResponse(manager);
    }

    @Transactional
    public DeliveryManagerResponse assign(DeliveryManagerAssignRequest request) {
        // MASTER, HUB_MANAGER만 배정 가능
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        DeliveryManager manager = deliveryManagerRepository
                .findFirstByManagerTypeAndStatusWithUser(request.getManagerType(), DeliveryManagerStatus.WAITING)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NO_AVAILABLE_DELIVERY_MANAGER));

        manager.updateLastAssigned();
        deliveryManagerRepository.save(manager);

        return new DeliveryManagerResponse(manager);
    }

    @Transactional
    public DeliveryManagerResponse updateStatus(UUID userId, DeliveryManagerStatusRequest request) {
        // MASTER, HUB_MANAGER만 상태 변경 가능
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        DeliveryManager manager = deliveryManagerRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        manager.updateStatus(request.getStatus());

        return new DeliveryManagerResponse(manager);
    }

    @Transactional
    public InternalDeliveryManagerResponse assignByHub(UUID fromHubId) {
        DeliveryManager manager = deliveryManagerRepository
                .findFirstByHubIdAndManagerTypeAndStatusWithUser(fromHubId, ManagerType.COMPANY_DELIVERY, DeliveryManagerStatus.WAITING)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NO_AVAILABLE_DELIVERY_MANAGER));

        manager.updateLastAssigned();
        deliveryManagerRepository.save(manager);

        return new InternalDeliveryManagerResponse(manager);
    }

}