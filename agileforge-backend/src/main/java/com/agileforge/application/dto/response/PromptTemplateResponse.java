package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PromptTemplateResponse(
        UUID id,
        UUID projectId,
        String name,
        String category,
        String template,
        String variables,
        boolean isGlobal,
        int usageCount,
        double rating,
        Instant createdAt
) {}
