package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestDescriptionRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 500)
        String title,

        @Size(max = 30)
        String type,

        @Size(max = 2000)
        String projectContext
) {}
