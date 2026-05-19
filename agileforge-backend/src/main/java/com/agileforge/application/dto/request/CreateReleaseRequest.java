package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReleaseRequest(
        @NotBlank(message = "Release name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Version is required")
        @Size(max = 50, message = "Version must not exceed 50 characters")
        String version,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        String startDate,

        String releaseDate
) {}
