package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WorkflowTransitionDto(
        @NotBlank(message = "From status is required")
        String fromStatus,

        @NotBlank(message = "To status is required")
        String toStatus
) {}
