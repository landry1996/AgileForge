package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyzeQualityRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 500)
        String title,

        @Size(max = 5000)
        String description,

        @Size(max = 30)
        String type
) {}
