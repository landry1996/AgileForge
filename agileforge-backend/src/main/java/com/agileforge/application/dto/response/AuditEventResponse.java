package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID organizationId,
        UUID projectId,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> details,
        String ipAddress,
        String severity,
        Instant createdAt
) {}
