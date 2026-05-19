package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix,
        List<String> permissions,
        Instant expiresAt,
        Instant lastUsedAt,
        boolean isActive,
        Instant createdAt
) {}
