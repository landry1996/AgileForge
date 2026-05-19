package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReleaseResponse(
        UUID id,
        UUID projectId,
        String name,
        String version,
        String description,
        String status,
        LocalDate startDate,
        LocalDate releaseDate,
        Instant releasedAt,
        int ticketCount,
        int completedCount,
        int progress,
        Instant createdAt,
        Instant updatedAt
) {}
