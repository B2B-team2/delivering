package com.sparta.hubservice.hubroute.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hubroute.application.dto.HubRouteCreateCommand;
import com.sparta.hubservice.hubroute.domain.port.HubInfo;
import com.sparta.hubservice.hubroute.domain.port.HubReader;
import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import com.sparta.hubservice.hubroute.application.dto.HubRouteUpdateCommand;
import com.sparta.hubservice.hubroute.application.dto.RouteSearchResult;
import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import com.sparta.hubservice.hubroute.domain.repository.HubRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteService {

    private static final BigDecimal DIRECT_THRESHOLD_KM = new BigDecimal("200");

    private final HubRouteRepository hubRouteRepository;
    private final HubReader hubReader;

    @Transactional
    public HubRouteDto createHubRoute(HubRouteCreateCommand command) {
        hubRouteRepository.findByFromHubIdAndToHubId(command.getFromHubId(), command.getToHubId())
                .ifPresent(r -> { throw new BusinessException(ErrorCode.DUPLICATE_ROUTE); });

        HubRoute hubRoute = HubRoute.builder()
                .fromHubId(command.getFromHubId())
                .toHubId(command.getToHubId())
                .duration(command.getDuration())
                .distance(command.getDistance())
                .build();
        HubRoute saved = hubRouteRepository.save(hubRoute);
        Map<UUID, HubInfo> hubInfos = hubReader.findAllHubInfos();
        return HubRouteDto.from(saved, hubInfos.get(saved.getFromHubId()), hubInfos.get(saved.getToHubId()));
    }

    public Page<HubRouteDto> getAllHubRoutes(Pageable pageable) {
        Map<UUID, HubInfo> hubInfos = hubReader.findAllHubInfos();
        return hubRouteRepository.findAll(pageable)
                .map(route -> HubRouteDto.from(
                        route,
                        hubInfos.get(route.getFromHubId()),
                        hubInfos.get(route.getToHubId())
                ));
    }

    public HubRouteDto getHubRoute(UUID routeId) {
        HubRoute hubRoute = hubRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        Map<UUID, HubInfo> hubInfos = hubReader.findAllHubInfos();
        return HubRouteDto.from(
                hubRoute,
                hubInfos.get(hubRoute.getFromHubId()),
                hubInfos.get(hubRoute.getToHubId())
        );
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
        Map<UUID, HubInfo> hubInfos = hubReader.findAllHubInfos();
        return HubRouteDto.from(hubRoute, hubInfos.get(hubRoute.getFromHubId()), hubInfos.get(hubRoute.getToHubId()));
    }

    @Transactional
    public void deleteHubRoute(UUID routeId, String deletedBy) {
        HubRoute hubRoute = hubRouteRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        hubRoute.softDelete(deletedBy);
        hubRouteRepository.save(hubRoute);
    }

    // TODO: Redis 연결 후 주석 해제
    // @Cacheable(value = "hubRoutes", key = "#fromHubId + ':' + #toHubId")
    public RouteSearchResult findRoute(UUID fromHubId, UUID toHubId) {
        Map<UUID, Map<UUID, HubRoute>> routeMap = hubRouteRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        HubRoute::getFromHubId,
                        Collectors.toMap(HubRoute::getToHubId, r -> r, (r1, r2) -> r1)
                ));

        Map<UUID, String> hubNames = hubReader.findAllHubNames();

        List<UUID> centralIds = hubReader.findCentralHubIds();

        List<HubRoute> path = buildPath(fromHubId, toHubId, routeMap, centralIds);
        return buildResult(fromHubId, toHubId, path, hubNames);
    }

    private List<HubRoute> buildPath(UUID fromId, UUID toId,
                                     Map<UUID, Map<UUID, HubRoute>> routeMap,
                                     List<UUID> availableCentralIds) {
        HubRoute direct = routeMap.getOrDefault(fromId, Map.of()).get(toId);
        if (direct == null) {
            throw new BusinessException(ErrorCode.ROUTE_NOT_FOUND);
        }

        // 200km 미만 → 직행
        if (direct.getDistance().compareTo(DIRECT_THRESHOLD_KM) < 0) {
            return List.of(direct);
        }

        // 200km 이상 → 사용 가능한 CENTRAL 중 duration 합산 최소인 허브 선택
        List<UUID> candidates = availableCentralIds.stream()
                .filter(id -> !id.equals(fromId) && !id.equals(toId))
                .toList();

        if (candidates.isEmpty()) {
            return List.of(direct);
        }

        UUID bestCentral = null;
        int bestCost = Integer.MAX_VALUE;

        for (UUID centralId : candidates) {
            HubRoute leg1 = routeMap.getOrDefault(fromId, Map.of()).get(centralId);
            HubRoute leg2 = routeMap.getOrDefault(centralId, Map.of()).get(toId);
            if (leg1 == null || leg2 == null) continue;

            int cost = leg1.getDuration() + leg2.getDuration();
            if (cost < bestCost) {
                bestCost = cost;
                bestCentral = centralId;
            }
        }

        if (bestCentral == null) {
            return List.of(direct);
        }

        // 선택된 CENTRAL은 하위 경로에서 재사용하지 않음
        final UUID chosen = bestCentral;
        List<UUID> remaining = candidates.stream()
                .filter(id -> !id.equals(chosen))
                .toList();

        List<HubRoute> result = new ArrayList<>();
        result.addAll(buildPath(fromId, chosen, routeMap, remaining));
        result.addAll(buildPath(chosen, toId, routeMap, remaining));
        return result;
    }

    private RouteSearchResult buildResult(UUID fromHubId, UUID toHubId,
                                          List<HubRoute> path, Map<UUID, String> hubNames) {
        int totalDuration = path.stream().mapToInt(HubRoute::getDuration).sum();
        BigDecimal totalDistance = path.stream()
                .map(HubRoute::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RouteSearchResult.Segment> segments = IntStream.range(0, path.size())
                .mapToObj(i -> {
                    HubRoute route = path.get(i);
                    return RouteSearchResult.Segment.builder()
                            .sequence(i + 1)
                            .routeId(route.getRouteId())
                            .fromHubId(route.getFromHubId())
                            .fromHubName(hubNames.getOrDefault(route.getFromHubId(), ""))
                            .toHubId(route.getToHubId())
                            .toHubName(hubNames.getOrDefault(route.getToHubId(), ""))
                            .duration(route.getDuration())
                            .distance(route.getDistance())
                            .build();
                })
                .toList();

        return RouteSearchResult.builder()
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .totalDuration(totalDuration)
                .totalDistance(totalDistance)
                .routes(segments)
                .build();
    }
}
