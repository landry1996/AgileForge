package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WebhookResponse(
        UUID id,
        UUID projectId,
        String url,
        List<String> events,
        boolean isActive,
        Instant lastTriggeredAt,
        int failureCount,
        Instant createdAt
) {}
