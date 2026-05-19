package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClientUserResponse(
        UUID id,
        UUID portalId,
        String email,
        String name,
        String company,
        boolean isActive,
        Instant lastLoginAt,
        Instant createdAt
) {}
