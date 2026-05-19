package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowStatusDto(
        @NotBlank(message = "Status name is required")
        @Size(max = 50, message = "Status name must not exceed 50 characters")
        String name,

        String category,

        int position,

        @Size(max = 7, message = "Color must not exceed 7 characters")
        String color
) {}
