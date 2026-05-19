package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePromptTemplateRequest(
        @NotBlank(message = "Template name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Category is required")
        String category,

        @NotBlank(message = "Template content is required")
        String template,

        String variables
) {}
