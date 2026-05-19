package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateIncidentRequest(
        @Size(min = 3, max = 300, message = "Title must be between 3 and 300 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        String severity,

        String status,

        UUID commanderId,

        String rootCause,

        String resolution,

        String postMortem
) {}
