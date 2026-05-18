package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 500, message = "Website URL must not exceed 500 characters")
        String website,

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        String logoUrl
) {}
