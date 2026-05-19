package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CapacityEntryResponse(
        UUID id,
        UUID projectId,
        UUID userId,
        String userName,
        UUID sprintId,
        double availableHours,
        double plannedLeaveHours,
        String notes,
        Instant createdAt
) {}
