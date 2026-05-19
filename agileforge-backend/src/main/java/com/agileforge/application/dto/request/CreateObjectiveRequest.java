package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateObjectiveRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 300, message = "Title must be between 2 and 300 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotBlank(message = "Period is required")
        @Size(max = 20, message = "Period must not exceed 20 characters")
        String period,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {}
