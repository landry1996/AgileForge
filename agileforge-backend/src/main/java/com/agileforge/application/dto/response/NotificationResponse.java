package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        UUID referenceId,
        String referenceType,
        boolean read,
        Instant createdAt
) {}
