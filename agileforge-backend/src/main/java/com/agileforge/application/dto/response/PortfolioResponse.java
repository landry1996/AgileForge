package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        UUID ownerId,
        List<PortfolioProjectSummary> projects,
        Instant createdAt
) {}
