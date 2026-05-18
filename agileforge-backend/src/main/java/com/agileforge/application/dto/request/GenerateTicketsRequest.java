package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateTicketsRequest(
        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
        String description,

        @Size(max = 2000)
        String projectContext
) {}
