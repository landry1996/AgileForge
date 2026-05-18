package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        UUID projectId,
        String name,
        String goal,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Integer capacity,
        long totalTickets,
        long doneTickets,
        Integer totalPoints,
        Instant createdAt
) {}
