package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateAcceptanceCriteriaRequest(
        @NotBlank(message = "Ticket title is required")
        @Size(max = 500)
        String ticketTitle,

        @Size(max = 5000)
        String ticketDescription,

        @Size(max = 30)
        String ticketType
) {}
