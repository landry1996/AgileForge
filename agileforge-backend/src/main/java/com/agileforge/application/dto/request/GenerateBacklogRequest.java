package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateBacklogRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 200)
        String projectName,

        @NotBlank(message = "Project description is required")
        @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
        String projectDescription,

        @Size(max = 50)
        String projectType,

        Integer maxTickets
) {}
