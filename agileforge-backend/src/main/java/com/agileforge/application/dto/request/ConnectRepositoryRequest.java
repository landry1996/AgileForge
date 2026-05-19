package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConnectRepositoryRequest(
        @NotBlank(message = "Owner is required")
        @Size(max = 100, message = "Owner must not exceed 100 characters")
        String owner,

        @NotBlank(message = "Repository name is required")
        @Size(max = 100, message = "Repository name must not exceed 100 characters")
        String repoName,

        @Size(max = 100, message = "Default branch must not exceed 100 characters")
        String defaultBranch,

        @Size(max = 500, message = "Access token must not exceed 500 characters")
        String accessToken
) {}
