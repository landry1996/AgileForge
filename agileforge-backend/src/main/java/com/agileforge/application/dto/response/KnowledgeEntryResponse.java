package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeEntryResponse(
        UUID id,
        UUID projectId,
        String category,
        String title,
        String content,
        String tags,
        boolean isActive,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
