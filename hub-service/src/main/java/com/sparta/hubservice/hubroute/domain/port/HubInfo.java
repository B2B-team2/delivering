package com.sparta.hubservice.hubroute.domain.port;

import java.util.UUID;

public record HubInfo(UUID hubId, String name, String address) {}
