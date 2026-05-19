package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SavedFilterResponse(
        UUID id,
        UUID projectId,
        UUID userId,
        String name,
        Map<String, Object> filterConfig,
        boolean isShared,
        Instant createdAt,
        Instant updatedAt
) {}
