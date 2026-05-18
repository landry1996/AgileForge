package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID projectId,
        String fullKey,
        String key,
        long number,
        String title,
        String description,
        String type,
        String status,
        String priority,
        UUID assigneeId,
        UUID reporterId,
        UUID epicId,
        UUID parentId,
        UUID sprintId,
        Integer storyPoints,
        Double estimatedHours,
        Double loggedHours,
        LocalDate dueDate,
        String environment,
        String component,
        String labels,
        String affectedVersion,
        String fixVersion,
        int qualityScore,
        Instant createdAt,
        Instant updatedAt
) {}
