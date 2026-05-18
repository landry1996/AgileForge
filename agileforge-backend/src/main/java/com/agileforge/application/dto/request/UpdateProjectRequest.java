package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        String type,

        String visibility,

        String status,

        String startDate,

        String endDate,

        String logoUrl
) {}
