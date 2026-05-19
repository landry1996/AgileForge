package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClientFeedbackResponse(
        UUID id,
        UUID portalId,
        UUID ticketId,
        UUID clientUserId,
        String clientName,
        String type,
        String content,
        Integer rating,
        Instant createdAt
) {}
