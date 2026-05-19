package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateKeyResultRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 300, message = "Title must be between 2 and 300 characters")
        String title,

        @NotNull(message = "Target value is required")
        Double targetValue,

        @Size(max = 50, message = "Unit must not exceed 50 characters")
        String unit,

        Double startValue
) {}
