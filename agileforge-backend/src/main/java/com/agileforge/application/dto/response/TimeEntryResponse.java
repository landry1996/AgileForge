package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID ticketId,
        UUID userId,
        double hours,
        String description,
        LocalDate workDate,
        Instant createdAt
) {}
