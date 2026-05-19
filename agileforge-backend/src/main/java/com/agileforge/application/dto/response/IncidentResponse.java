package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        String severity,
        String status,
        UUID commanderId,
        Instant startedAt,
        Instant resolvedAt,
        String rootCause,
        String resolution,
        String postMortem,
        List<UUID> participantIds,
        Long durationMinutes,
        Instant createdAt
) {}
