package com.sparta.hubservice.hub.application.service;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.client.CompanyClient;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;
    private final CompanyClient companyClient;

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

    public Page<HubDto> getAllHubs(String hubType, String status, String keyword, Pageable pageable) {
        HubType hubTypeEnum = (hubType != null) ? HubType.valueOf(hubType) : null;
        HubStatus statusEnum = (status != null) ? HubStatus.valueOf(status) : null;
        return hubRepository.search(hubTypeEnum, statusEnum, keyword, pageable).map(HubDto::from);
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
    public void deleteHub(UUID hubId, UUID deletedBy) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));

        ApiResponse<Boolean> companyCheck = companyClient.existsCompaniesByHubId(hubId);
        if (Boolean.TRUE.equals(companyCheck.getData())) {
            throw new BusinessException(ErrorCode.HUB_IN_USE);
        }

        hub.softDelete(deletedBy);
        hubRepository.save(hub);
    }
}
