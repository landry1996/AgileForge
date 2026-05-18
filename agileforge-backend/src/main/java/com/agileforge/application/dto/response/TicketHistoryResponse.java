package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TicketHistoryResponse(
        UUID id,
        UUID ticketId,
        UUID userId,
        String field,
        String oldValue,
        String newValue,
        Instant createdAt
) {}
