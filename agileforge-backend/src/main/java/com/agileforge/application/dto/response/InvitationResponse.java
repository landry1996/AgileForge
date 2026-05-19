package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID organizationId,
        String email,
        String role,
        String status,
        UUID invitedBy,
        Instant expiresAt,
        Instant createdAt
) {}
