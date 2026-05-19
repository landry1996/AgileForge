package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLabelRequest(
        @NotBlank(message = "Label name is required")
        @Size(max = 50, message = "Label name must not exceed 50 characters")
        String name,

        @Size(max = 7, message = "Color must be a valid hex color code")
        String color,

        @Size(max = 200, message = "Description must not exceed 200 characters")
        String description
) {}
