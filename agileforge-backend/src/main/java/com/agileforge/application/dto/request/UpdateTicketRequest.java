package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTicketRequest(
        @Size(min = 5, max = 500, message = "Title must be between 5 and 500 characters")
        String title,

        String description,

        String type,

        String status,

        String priority,

        UUID assigneeId,

        UUID epicId,

        UUID parentId,

        UUID sprintId,

        Integer storyPoints,

        Double estimatedHours,

        String dueDate,

        String environment,

        String component,

        String labels,

        String affectedVersion,

        String fixVersion
) {}
