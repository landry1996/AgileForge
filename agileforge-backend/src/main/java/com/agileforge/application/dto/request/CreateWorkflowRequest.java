package com.agileforge.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateWorkflowRequest(
        @NotBlank(message = "Workflow name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Ticket type is required")
        @Size(max = 30, message = "Ticket type must not exceed 30 characters")
        String ticketType,

        @Valid
        List<WorkflowStatusDto> statuses,

        @Valid
        List<WorkflowTransitionDto> transitions
) {}
