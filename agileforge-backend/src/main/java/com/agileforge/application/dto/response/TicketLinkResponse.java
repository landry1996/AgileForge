package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TicketLinkResponse(
        UUID id,
        UUID sourceTicketId,
        UUID targetTicketId,
        String linkType,
        String sourceTicketKey,
        String targetTicketKey,
        Instant createdAt
) {}
