package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String displayName,
        String avatarUrl,
        String phone,
        String timezone,
        String locale,
        boolean active,
        boolean emailVerified,
        Instant lastLoginAt,
        Instant createdAt
) {}
