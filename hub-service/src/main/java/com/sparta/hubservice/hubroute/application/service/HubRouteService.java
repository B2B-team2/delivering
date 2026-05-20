package com.sparta.hubservice.hubroute.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hubroute.application.dto.HubRouteCreateCommand;
import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import com.sparta.hubservice.hubroute.application.dto.HubRouteUpdateCommand;
import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import com.sparta.hubservice.hubroute.domain.repository.HubRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteService {

    private final HubRouteRepository hubRouteRepository;

    @Transactional
    public HubRouteDto createHubRoute(HubRouteCreateCommand command) {
        HubRoute hubRoute = HubRoute.builder()
                .fromHubId(command.getFromHubId())
                .toHubId(command.getToHubId())
                .duration(command.getDuration())
                .distance(command.getDistance())
                .build();
        return HubRouteDto.from(hubRouteRepository.save(hubRoute));
    }

    public HubRouteDto getHubRoute(UUID routeId) {
        HubRoute hubRoute = hubRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        return HubRouteDto.from(hubRoute);
    }

    public List<HubRouteDto> getRoutesByHub(UUID fromHubId) {
        return hubRouteRepository.findByFromHubId(fromHubId).stream()
                .map(HubRouteDto::from)
                .toList();
    }

    @Transactional
    public HubRouteDto updateHubRoute(UUID routeId, HubRouteUpdateCommand command) {
        HubRoute hubRoute = hubRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        hubRoute.update(command.getDuration(), command.getDistance());
        return HubRouteDto.from(hubRoute);
    }

    @Transactional
    public void deleteHubRoute(UUID routeId, String deletedBy) {
        HubRoute hubRoute = hubRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        hubRoute.softDelete(deletedBy);
        hubRouteRepository.save(hubRoute);
    }
}
