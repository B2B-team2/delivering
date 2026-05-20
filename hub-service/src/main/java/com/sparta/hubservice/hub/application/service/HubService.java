package com.sparta.hubservice.hub.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hub.application.dto.HubCreateCommand;
import com.sparta.hubservice.hub.application.dto.HubDto;
import com.sparta.hubservice.hub.application.dto.HubUpdateCommand;
import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
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
    public HubDto createHub(HubCreateCommand command) {
        Hub hub = Hub.builder()
                .name(command.getName())
                .hubType(HubType.valueOf(command.getHubType()))
                .address(command.getAddress())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .contactPhone(command.getContactPhone())
                .status(HubStatus.ACTIVE)
                .build();
        return HubDto.from(hubRepository.save(hub));
    }

    @Cacheable(value = "hubs", key = "'all'")
    public List<HubDto> getAllHubs() {
        return hubRepository.findAll().stream()
                .map(HubDto::from)
                .toList();
    }

    @Cacheable(value = "hubs", key = "#hubId")
    public HubDto getHub(UUID hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
        return HubDto.from(hub);
    }

    @Transactional
    @CacheEvict(value = "hubs", allEntries = true)
    public HubDto updateHub(UUID hubId, HubUpdateCommand command) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
        HubStatus status = command.getStatus() != null ? HubStatus.valueOf(command.getStatus()) : null;
        hub.update(command.getName(), command.getAddress(), command.getLatitude(),
                command.getLongitude(), command.getContactPhone(), status);
        return HubDto.from(hub);
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
