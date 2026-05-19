package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTicketLinkRequest(
        @NotNull(message = "Target ticket ID is required")
        UUID targetTicketId,

        @NotBlank(message = "Link type is required")
        String linkType
) {}
