package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LabelResponse(
        UUID id,
        UUID projectId,
        String name,
        String color,
        String description,
        Instant createdAt
) {}
