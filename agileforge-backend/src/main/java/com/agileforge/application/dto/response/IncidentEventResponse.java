package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record IncidentEventResponse(
        UUID id,
        UUID incidentId,
        UUID userId,
        String eventType,
        String message,
        Instant createdAt
) {}
