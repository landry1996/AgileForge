package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID organizationId,
        String name,
        String key,
        String description,
        String logoUrl,
        String type,
        String visibility,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        UUID leadId,
        Instant createdAt
) {}
