package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID documentId,
        String title,
        int version,
        UUID editedBy,
        String changeSummary,
        Instant createdAt
) {}
