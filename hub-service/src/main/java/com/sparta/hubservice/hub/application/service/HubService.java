package com.sparta.hubservice.hub.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
import com.sparta.hubservice.hub.presentation.dto.HubCreateRequest;
import com.sparta.hubservice.hub.presentation.dto.HubResponse;
import com.sparta.hubservice.hub.presentation.dto.HubUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;

    @Transactional
    public HubResponse createHub(HubCreateRequest request) {
        Hub hub = Hub.builder()
                .name(request.name())
                .hubType(request.hubType())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .contactPhone(request.contactPhone())
                .status(HubStatus.ACTIVE)
                .build();
        return HubResponse.from(hubRepository.save(hub));
    }

    @Cacheable(value = "hubs", key = "'all'")
    public List<HubResponse> getAllHubs() {
        return hubRepository.findAll().stream()
                .map(HubResponse::from)
                .toList();
    }

    @Cacheable(value = "hubs", key = "#hubId")
    public HubResponse getHub(UUID hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
        return HubResponse.from(hub);
    }

    @Transactional
    @CacheEvict(value = "hubs", allEntries = true)
    public HubResponse updateHub(UUID hubId, HubUpdateRequest request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
        hub.update(request.name(), request.address(), request.latitude(), request.longitude(),
                request.contactPhone(), request.status());
        return HubResponse.from(hub);
    }

    @Transactional
    @CacheEvict(value = "hubs", allEntries = true)
    public void deleteHub(UUID hubId, String deletedBy) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
        hub.softDelete(deletedBy);
        hubRepository.save(hub);
    }
}
