package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID userId,
        String email,
        String displayName,
        String avatarUrl,
        String roleCode,
        String roleName,
        Instant joinedAt,
        boolean active
) {}
