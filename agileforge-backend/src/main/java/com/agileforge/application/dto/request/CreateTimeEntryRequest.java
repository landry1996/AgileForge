package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTimeEntryRequest(
        @NotNull(message = "Hours is required")
        @Positive(message = "Hours must be positive")
        Double hours,

        String description,

        LocalDate workDate
) {}
