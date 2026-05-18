package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String logoUrl,
        String website,
        String plan,
        int maxUsers,
        int maxProjects,
        boolean active,
        Instant createdAt
) {}
