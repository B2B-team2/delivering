package com.sparta.hubservice.hubroute.domain.port;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HubReader {
    Map<UUID, String> findAllHubNames();
    List<UUID> findCentralHubIds();
}
