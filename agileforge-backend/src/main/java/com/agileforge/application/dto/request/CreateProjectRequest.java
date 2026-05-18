package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @NotBlank(message = "Project key is required")
        @Size(min = 2, max = 10, message = "Key must be between 2 and 10 characters")
        @Pattern(regexp = "^[A-Z][A-Z0-9]+$", message = "Key must start with a letter and contain only uppercase letters and numbers")
        String key,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Project type is required")
        String type,

        String visibility,

        String startDate,

        String endDate
) {}
