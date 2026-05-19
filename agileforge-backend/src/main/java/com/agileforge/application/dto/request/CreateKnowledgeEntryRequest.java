package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateKnowledgeEntryRequest(
        @NotBlank(message = "Category is required")
        @Size(max = 50)
        String category,

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @NotBlank(message = "Content is required")
        String content,

        @Size(max = 500)
        String tags
) {}
