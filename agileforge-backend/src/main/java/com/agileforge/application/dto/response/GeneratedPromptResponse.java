package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GeneratedPromptResponse(
        UUID id,
        UUID ticketId,
        String promptText,
        String templateName,
        Instant createdAt
) {}
