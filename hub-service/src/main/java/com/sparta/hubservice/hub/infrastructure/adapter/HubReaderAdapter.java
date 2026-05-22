package com.sparta.hubservice.hub.infrastructure.adapter;

import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
import com.sparta.hubservice.hubroute.domain.port.HubReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HubReaderAdapter implements HubReader {

    private final HubRepository hubRepository;

    @Override
    public Map<UUID, String> findAllHubNames() {
        return hubRepository.findAll().stream()
                .collect(Collectors.toMap(hub -> hub.getHubId(), hub -> hub.getName()));
    }

    @Override
    public List<UUID> findCentralHubIds() {
        return hubRepository.findAllByHubType(HubType.CENTRAL).stream()
                .map(hub -> hub.getHubId())
                .toList();
    }
}
