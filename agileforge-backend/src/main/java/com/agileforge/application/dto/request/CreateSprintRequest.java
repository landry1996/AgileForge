package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSprintRequest(
        @NotBlank(message = "Sprint name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @Size(max = 2000, message = "Goal must not exceed 2000 characters")
        String goal,

        String startDate,

        String endDate,

        Integer capacity
) {}
