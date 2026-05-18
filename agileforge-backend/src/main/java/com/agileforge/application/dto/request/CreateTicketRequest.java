package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 500, message = "Title must be between 5 and 500 characters")
        String title,

        String description,

        @NotNull(message = "Type is required")
        String type,

        String priority,

        UUID assigneeId,

        UUID epicId,

        UUID parentId,

        Integer storyPoints,

        Double estimatedHours,

        String dueDate,

        String environment,

        String component,

        String labels
) {}
