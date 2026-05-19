package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID projectId,
        UUID parentId,
        String title,
        String content,
        String docType,
        String status,
        int position,
        UUID authorId,
        UUID lastEditedBy,
        int version,
        Instant createdAt,
        Instant updatedAt,
        List<UUID> linkedTicketIds
) {}
