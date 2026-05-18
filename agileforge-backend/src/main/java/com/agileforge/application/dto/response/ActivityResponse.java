package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID ticketId,
        String ticketKey,
        UUID userId,
        String userName,
        String action,
        String field,
        String oldValue,
        String newValue,
        Instant createdAt
) {}
