package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateCapacityEntryRequest(
        @NotNull UUID userId,
        UUID sprintId,
        @NotNull @Positive Double availableHours,
        Double plannedLeaveHours,
        String notes
) {}
